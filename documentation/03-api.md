# Контракт Diploma API

Актуальный источник: [android-diploma.education-services.ru/docs](https://android-diploma.education-services.ru/docs). Старые описания прямого HeadHunter API не используются.

## 1. Подключение

```text
Base URL: https://android-diploma.education-services.ru
Response format: application/json
Required header: Authorization: Bearer <token>
```

Web-документация показывает raw-токен и обещает `403 Forbidden`, но live smoke-check 11.08.2026 подтвердил другое поведение:

- валидный raw-токен → `401 Unauthorized`;
- валидный `Bearer <token>` → `200 OK`;
- отсутствующий или некорректный заголовок → `401 Unauthorized` с `WWW-Authenticate: Bearer`.

Поэтому клиент добавляет префикс `Bearer`. Фактический контракт имеет приоритет над устаревшим примером страницы.

### Безопасная конфигурация

Локальный файл, исключённый из Git:

```properties
# develop.properties
apiAccessToken=replace_locally
```

Обычный CI собирает проверочный APK с пустым токеном. Если позже добавляется live API smoke с GitHub Actions secret, он должен выполняться отдельным защищённым job без публикации APK. Реальное значение нельзя помещать в Markdown, `gradle.properties`, committed `local.properties`, исходный код, логи OkHttp или скриншоты.

## 2. Endpoint

| Метод и путь | Назначение | Параметры | Успех | Ошибки |
|---|---|---|---|---|
| `GET /areas` | Дерево регионов | нет | `200 Array<FilterArea>` | `401` фактически; `500` по документации. |
| `GET /industries` | Плоский список отраслей | нет | `200 Array<FilterIndustry>` | `401` фактически; `500` по документации. |
| `GET /vacancies` | Поиск вакансий | `area?`, `industry?`, `text?`, `salary?`, `page?`, `only_with_salary?` | `200 VacancyResponse` | `401` фактически; `500` по документации. |
| `GET /vacancies/{id}` | Детали вакансии | path `id` | `200 VacancyDetail` | `401`, `404`, `500`. |

Хотя API помечает `text` как параметр запроса, продукт отправляет поиск только для непустого текста.

## 3. Поиск

Пример без секрета:

```http
GET /vacancies?text=android&industry=7&salary=100000&page=1&only_with_salary=true
Authorization: Bearer <token-from-BuildConfig>
```

Пустые фильтры в URL не передаются. Параметр `per_page` в актуальном proxy API не документирован и добавляться не должен. Live smoke подтвердил: первая страница имеет номер `1`, содержит до 20 элементов, а `page=0` нормализуется сервером в `page=1`.

### Ответ

```json
{
  "found": 0,
  "pages": 0,
  "page": 0,
  "items": [
    {
      "id": "string",
      "name": "string",
      "company": "string or null",
      "city": "string or null",
      "salary": {
        "from": 100000,
        "to": 200000,
        "currency": "RUR"
      },
      "logo": "https://... or null"
    }
  ]
}
```

Схема:

```text
VacancyResponse {
  found: integer
  pages: integer
  page: integer
  items: Array<VacancyCard>
}

VacancyCard {
  id: string
  name: string
  company: string?
  city: string?
  salary: VacancyCardSalary?
  logo: string?
}

VacancyCardSalary {
  from: integer?
  to: integer?
  currency: string?
}
```

Поддерживаемые коды валют: `RUR`, `RUB`, `BYR`, `USD`, `EUR`, `KZT`, `UAH`, `AZN`, `UZS`, `GEL`, `KGT`. Неизвестный код не должен приводить к сбою: до уточнения он показывается как полученная строка.

## 4. Детали

```text
GET /vacancies/{id}
```

```text
VacancyDetail {
  id: string
  name: string
  description: string              // HTML
  salary: Salary?
  address: Address?
  experience: Experience?
  schedule: Schedule?
  employment: Employment?
  contacts: Contacts?
  employer: Employer
  area: FilterArea
  skills: Array<string>
  url: string
  industry: FilterIndustry
}

Salary {
  from: integer?
  to: integer?
  currency: string?
}

Address {
  id: string
  city: string
  street: string
  building: string
  raw: string
}

Experience { id: string, name: string }
Schedule   { id: string, name: string }
Employment { id: string, name: string }

Contacts {
  id: string
  name: string
  email: string
  phones: Array<Phone>
}

Phone {
  comment: string?
  formatted: string
}

Employer {
  id: string
  name: string
  logo: string
}
```

ТЗ предупреждает, что части деталей могут отсутствовать. Поэтому клиентские DTO для вложенных объектов и их полей должны быть null-safe даже там, где краткая web-схема не ставит `?`. Это защитная клиентская трактовка, а не изменение API.

`description` — HTML. Его нужно безопасно преобразовать средствами Android в форматированный текст; HTML нельзя конкатенировать в WebView с выполнением скриптов.

## 5. Каталоги

### Отрасли

```text
FilterIndustry {
  id: integer
  name: string
}
```

Актуальный endpoint возвращает плоский массив; иерархия старого HH API не используется.

### Регионы

```text
FilterArea {
  id: integer
  name: string
  parentId: integer?
  areas: Array<FilterArea>
}
```

В web-документации `parentId` указан как integer, однако live response вернул девять корневых элементов и у всех `parentId = null`. В DTO поле обязательно nullable.

## 6. Retrofit-интерфейс — ориентир

```kotlin
interface DiplomaApi {
    @GET("areas")
    suspend fun getAreas(): List<AreaDto>

    @GET("industries")
    suspend fun getIndustries(): List<IndustryDto>

    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") text: String,
        @Query("area") area: Int? = null,
        @Query("industry") industry: Int? = null,
        @Query("salary") salary: Int? = null,
        @Query("page") page: Int? = null,
        @Query("only_with_salary") onlyWithSalary: Boolean? = null,
    ): VacancyResponseDto

    @GET("vacancies/{id}")
    suspend fun getVacancy(@Path("id") id: String): VacancyDetailDto
}
```

Authorization добавляется единым OkHttp interceptor. В production-коде не должно быть ручной передачи токена из каждого repository.

## 7. Ошибки и клиентская реакция

| Ситуация | Domain error | Поведение |
|---|---|---|
| Нет подключения до запроса | `NoInternet` | Специальный плейсхолдер; при paging — Toast и сохранение списка. |
| Explicit `NoInternetException` | Feature `NoInternet` | Показать состояние отсутствия сети; не падать. |
| Прочие IOException/timeout | Feature `Generic`/`Server` | Не выдавать таймаут или сбой DNS за доказанное отсутствие сети. |
| HTTP 401 | `Unauthorized` | Ошибка заголовка/токена; не печатать токен. |
| HTTP 403 | `Unauthorized` | Сохранить обработку на случай поведения, заявленного web-документацией. |
| HTTP 404 деталей | `NotFound` | Показать ошибку и удалить эту вакансию из Room. |
| HTTP 500 | `Server` | Общий серверный плейсхолдер/Toast. |
| Ошибка JSON/mapping | `Unknown` | Зафиксировать без чувствительных данных, показать общую ошибку. |

## 8. Проверки контракта до реализации фич

Live smoke-check 11.08.2026 уже подтвердил:

1. Рабочий формат — `Authorization: Bearer <token>`; raw-токен не работает.
2. Первая страница — `1`; запрос `page=0` возвращает `page=1`.
3. Поисковая страница содержит 20 элементов.
4. У корневых `areas` поле `parentId` равно `null`.
5. Несуществующий ID деталей возвращает `404`.
6. Пять проверенных Android-вакансий содержали все 14 верхнеуровневых detail-полей, но клиент всё равно следует требованию ТЗ о возможной опциональности.

Задача E0-08 должна превратить эти проверки в воспроизводимый безопасный smoke/regression check и дополнительно проверить `500`/неполные fixtures без публикации токена или полного payload.

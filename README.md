# Android-приложение для поиска вакансий

Дипломный командный проект Яндекс Практикума. Основа Android-проекта импортирована из официального репозитория [Yandex-Practicum/practicum-android-diploma](https://github.com/Yandex-Practicum/practicum-android-diploma); проектная документация и декомпозиция подготовлены для команды из трёх человек.

Ветка `iteration_2_review` готовится как накопительный инкремент: принятый фундамент Epic 0 дополняется поиском, экраном команды, деталями и избранными вакансиями. Реализация и формальная проверка ведутся по задачам из [декомпозиции](./documentation/05-декомпозиция.md); функциональные фильтры остаются для третьей итерации.

## MVP

- поиск вакансий с debounce и пагинацией;
- фильтры по отрасли и зарплате;
- экран подробной информации о вакансии;
- локальное избранное и офлайн-просмотр;
- светлая и тёмная темы;
- экран команды и обработка состояний загрузки и ошибок.

Фильтрация по стране и региону вынесена в stretch goal.

## Команда и декомпозиция

| Роль | MVP | Stretch |
|---|---:|---:|
| UI / дизайнер-разработчик | 17 задач, 58 SP | 2 задачи, 10 SP |
| BE-1 / поиск и фильтры | 17 задач, 58 SP | 2 задачи, 8 SP |
| BE-2 / детали и избранное | 15 задач, 58 SP | 2 задачи, 8 SP |
| **Итого** | **49 задач, 174 SP** | **6 задач, 26 SP** |

Карточки распределены по ролям на [доске Trello](https://trello.com/b/GhZWYEH2/моя-доска-trello). Каждая карточка содержит ID, итерацию и оценку в Story Points.

## Порядок сдачи на ревью

| Формальная итерация | Scope |
|---|---|
| `iteration_1_review` | Только Epic 0: проект и окружение, общая инфраструктура, темы, навигация и экраны-заглушки. |
| `iteration_2_review` | Поиск, экран команды, детали вакансии и избранные вакансии. |
| `iteration_3_review` | Функционал фильтров. |

Описания формальных PR находятся в документах [первой](./documentation/09-iteration-1-review.md) и [второй](./documentation/10-iteration-2-review.md) итераций. Функции следующего этапа не добавляются в более раннюю review-ветку.

## Быстрый старт

Для сборки нужны Android Studio и JDK 21 для Gradle Daemon. Исходный код приложения компилируется с target JVM 17.

1. Клонируйте репозиторий:

   ```bash
   git clone https://github.com/iudicio/yandex_team_project.git
   cd yandex_team_project
   ```

2. Скопируйте `develop.properties.example` в `develop.properties` и укажите токен:

   ```properties
   apiAccessToken=<ваш_токен>
   ```

3. Синхронизируйте Gradle и соберите приложение:

   ```bash
   ./gradlew assembleDebug
   ```

   В Windows используйте `gradlew.bat assembleDebug`.

`develop.properties` добавлен в `.gitignore` и не должен попадать в коммиты.

## Проверки качества

```bash
./gradlew test
./gradlew detektAll
./gradlew assembleDebug
```

Автоисправление поддерживаемых замечаний форматирования:

```bash
./gradlew detektFormat
```

Конфигурация detekt находится в [`conf/detekt.yml`](./conf/detekt.yml), CI — в [`.github/workflows/pr_checks.yml`](./.github/workflows/pr_checks.yml).

CI намеренно собирает проверочный APK с пустым токеном: реальный секрет не попадает в публикуемый артефакт. Локальный токен нельзя помещать в исходный код, Markdown, логи или committed-конфигурацию.

## Структура репозитория

| Путь | Назначение |
|---|---|
| [`app`](./app) | Android-приложение и тесты |
| [`build-logic`](./build-logic) | Gradle convention plugins |
| [`documentation`](./documentation) | Проектная документация команды |
| [`Diploma`](./Diploma) | Исходные учебные материалы и критерии |
| [`conf`](./conf) | Настройки статического анализа |
| [`docs`](./docs) | Иллюстрации для инструкций проекта |

## Документация

Начальная точка — [оглавление документации](./documentation/README.md).

- [Источники и приоритеты](./documentation/00-источники.md)
- [Продукт и требования](./documentation/01-продукт-и-требования.md)
- [Архитектура](./documentation/02-архитектура.md)
- [Контракт API](./documentation/03-api.md)
- [UI/UX-спецификация](./documentation/04-ui-ux.md)
- [Декомпозиция проекта](./documentation/05-декомпозиция.md)
- [Процесс разработки](./documentation/06-процесс-разработки.md)
- [План тестирования](./documentation/07-тест-план.md)
- [Решения, риски и открытые вопросы](./documentation/08-решения-и-риски.md)
- [Описание первой итерации ревью](./documentation/09-iteration-1-review.md)
- [Описание второй итерации ревью](./documentation/10-iteration-2-review.md)

## Основные источники

- [Официальный стартовый проект](https://github.com/Yandex-Practicum/practicum-android-diploma)
- [Техническое задание](https://github.com/Yandex-Practicum/practicum-android-diploma/blob/main/%D0%A2%D0%B5%D1%85%D0%BD%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%BE%D0%B5%20%D0%B7%D0%B0%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5.md)
- [Документация Diploma API](https://android-diploma.education-services.ru/docs)
- [Макеты Figma](https://www.figma.com/design/2xNhuofNp4bedjPLoQdBfC/%D0%94%D0%B8%D0%BF%D0%BB%D0%BE%D0%BC%D0%BD%D1%8B%D0%B9-%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82--Copy-?node-id=0-1&p=f&t=Jx4VVgkNreueEhI9-0)

## Безопасность

API-токен не хранится в Git. Документация описывает безопасную локальную настройку; обычный CI и его APK-артефакт используют пустое значение.

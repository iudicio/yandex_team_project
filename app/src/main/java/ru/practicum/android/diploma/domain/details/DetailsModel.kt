package ru.practicum.android.diploma.domain.details

import ru.practicum.android.diploma.ui.details.VacancyDetailsMock

interface DetailsModel {
    var vacancyId: String
    var isFavorite: Boolean
    fun getVacancy(): VacancyDetailsMock
    fun toggleFavorite(): Boolean
}

class InMemoryDetailsModel(
    override var vacancyId: String = "",
    override var isFavorite: Boolean = false,
) : DetailsModel {

    override fun getVacancy(): VacancyDetailsMock {
        // Пока мок-данные
        return VacancyDetailsMock(
            id = vacancyId,
            title = "Android-разработчик",
            salary = "от 100 000 до 150 000 ₽",
            employerName = "Яндекс",
            employerLogoUrl = null,
            city = "Москва",
            experience = "От 1 года до 3 лет",
            employment = "Полная занятость",
            schedule = "Удаленная работа",
            description = """
                <h2>Описание вакансии</h2>
                <p>Ищем Android-разработчика в продуктовую команду мобильного приложения с активной пользовательской базой.</p>
                <p>Важно, чтобы кандидат уверенно работал с Kotlin и мог доводить задачи до продакшена без лишних согласований.</p>
                <section>
                    <h3>Обязанности</h3>
                    <ul>
                        <li>Разрабатывать новую функциональность приложения и улучшать текущие пользовательские сценарии.</li>
                        <li>Проектировать клиент-серверное взаимодействие совместно с backend-командой.</li>
                        <li>Поддерживать качество кода, писать unit- и UI-тесты.</li>
                        <li>Участвовать в code review и техническом планировании.</li>
                    </ul>
                </section>
                <section>
                    <h3>Требования</h3>
                    <ul>
                        <li>Коммерческий опыт Android-разработки от 1 года.</li>
                        <li>Уверенное знание Kotlin, Coroutines и Flow.</li>
                        <li>Опыт работы с Android SDK, архитектурой MVVM или MVI.</li>
                        <li>Понимание принципов SOLID и чистой архитектуры.</li>
                    </ul>
                </section>
                <section>
                    <h3>Условия</h3>
                    <ul>
                        <li>Полная занятость и удаленный формат работы.</li>
                        <li>Адекватные сроки, прозрачная постановка задач и регулярная обратная связь.</li>
                        <li>Бюджет на обучение, внутренние митапы и ревью архитектурных решений.</li>
                    </ul>
                </section>
            """.trimIndent(),
            keySkills = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Dagger/Hilt"),
            contactName = "Иван Иванов",
            contactEmail = "ivan@yandex.ru",
            contactPhone = "+7 (999) 000-00-00",
            contactComment = "Звонить с 10:00 до 19:00"
        )
    }

    override fun toggleFavorite(): Boolean {
        isFavorite = !isFavorite
        return isFavorite
    }
}

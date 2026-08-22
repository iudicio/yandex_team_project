package ru.practicum.android.diploma.domain.details

interface VacancyDetailRepository {
    suspend fun getVacancy(vacancyId: String): VacancyDetailsOutcome
}

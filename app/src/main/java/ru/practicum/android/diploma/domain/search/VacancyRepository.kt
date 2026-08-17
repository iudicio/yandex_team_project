package ru.practicum.android.diploma.domain.search

interface VacancyRepository {
    suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult>
}

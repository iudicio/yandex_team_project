package ru.practicum.android.diploma.domain.search

interface SearchVacanciesInteractor {
    suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult>
}

class SearchVacanciesInteractorImpl(
    private val repository: VacancyRepository,
) : SearchVacanciesInteractor {

    override suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult> =
        repository.search(request)
}

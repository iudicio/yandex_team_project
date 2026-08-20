package ru.practicum.android.diploma.domain.search

interface SearchVacanciesInteractor {
    suspend fun search(request: VacancySearchRequest): SearchOutcome<VacancySearchPage>
}

class SearchVacanciesInteractorImpl(
    private val repository: VacancyRepository,
) : SearchVacanciesInteractor {
    override suspend fun search(request: VacancySearchRequest): SearchOutcome<VacancySearchPage> =
        repository.search(request)
}

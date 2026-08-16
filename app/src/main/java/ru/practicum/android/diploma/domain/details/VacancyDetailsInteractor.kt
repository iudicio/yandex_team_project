package ru.practicum.android.diploma.domain.details

interface VacancyDetailsInteractor {
    suspend fun getDetails(request: String): Result<VacancyDetailResult>
}

class VacancyDetailsInteractorImpl (private val repository: VacancyDetailRepository): VacancyDetailsInteractor {

    override suspend fun getDetails(request: String): Result<VacancyDetailResult> {
        return repository.getVacancy(request)
    }
}

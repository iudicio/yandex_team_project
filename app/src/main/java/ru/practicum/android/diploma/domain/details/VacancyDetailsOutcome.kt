package ru.practicum.android.diploma.domain.details

sealed interface VacancyDetailsError {
    data object NoInternet : VacancyDetailsError

    data object NotFound : VacancyDetailsError

    data object Server : VacancyDetailsError
}

sealed interface VacancyDetailsOutcome {
    data class Content(
        val vacancy: VacancyDetailResult,
        val isOffline: Boolean = false,
    ) : VacancyDetailsOutcome

    data class Failure(
        val error: VacancyDetailsError,
    ) : VacancyDetailsOutcome
}

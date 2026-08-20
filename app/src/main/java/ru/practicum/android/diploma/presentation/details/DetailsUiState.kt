package ru.practicum.android.diploma.presentation.details

import ru.practicum.android.diploma.domain.details.VacancyDetailResult

sealed interface DetailsError {
    data object NoInternet : DetailsError

    data object NotFound : DetailsError

    data object Server : DetailsError
}

data class DetailsUiState(
    val isLoading: Boolean = true,
    val vacancy: VacancyDetailResult? = null,
    val isOffline: Boolean = false,
    val isFavorite: Boolean = false,
    val error: DetailsError? = null,
)

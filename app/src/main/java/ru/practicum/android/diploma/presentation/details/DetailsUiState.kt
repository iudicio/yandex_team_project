package ru.practicum.android.diploma.presentation.details

import ru.practicum.android.diploma.domain.details.VacancyDetailResult

data class DetailsUiState(
    val isLoading: Boolean = false,
    val vacancy: VacancyDetailResult? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

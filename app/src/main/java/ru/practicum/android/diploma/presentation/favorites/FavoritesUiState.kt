package ru.practicum.android.diploma.presentation.favorites

import ru.practicum.android.diploma.domain.search.VacancyCard

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data object Empty : FavoritesUiState

    data object DatabaseError : FavoritesUiState

    data class Content(
        val vacancies: List<VacancyCard>,
    ) : FavoritesUiState
}

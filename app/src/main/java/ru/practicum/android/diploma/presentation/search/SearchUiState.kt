package ru.practicum.android.diploma.presentation.search

import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.VacancyCard

data class SearchUiState(
    val query: String = "",
    val result: SearchResultUiState = SearchResultUiState.Initial,
    val hasActiveFilters: Boolean = false,
)

sealed interface SearchResultUiState {
    data object Initial : SearchResultUiState
    data object Loading : SearchResultUiState

    data class Content(
        val found: Int,
        val items: List<VacancyCard>,
        val lastLoadedPage: Int,
        val isLoadingNextPage: Boolean = false,
    ) : SearchResultUiState

    data object Empty : SearchResultUiState
    data object NoInternet : SearchResultUiState
    data object GenericError : SearchResultUiState
}

sealed interface SearchEvent {
    data class PagingFailed(val error: SearchError) : SearchEvent
}

package ru.practicum.android.diploma.presentation.search

import ru.practicum.android.diploma.domain.search.VacancyCard
import java.io.IOException

data class SearchUiState(
    val query: String,
    val result: SearchResultUiState = SearchResultUiState.Initial,
) {
    val action: SearchAction = if (query.isEmpty()) SearchAction.SEARCH else SearchAction.CLEAR
}

sealed interface SearchResultUiState {
    data object Initial : SearchResultUiState

    data object Loading : SearchResultUiState

    data class Content(
        val found: Int,
        val items: List<VacancyCard>,
        val isLoadingNextPage: Boolean = false,
    ) : SearchResultUiState

    data object Empty : SearchResultUiState

    data class NoInternet(
        val cause: IOException,
    ) : SearchResultUiState

    data class Error(
        val cause: Exception,
    ) : SearchResultUiState
}

sealed interface SearchEvent {
    data class PagingFailed(
        val cause: Exception,
    ) : SearchEvent
}

enum class SearchAction {
    SEARCH,
    CLEAR,
}

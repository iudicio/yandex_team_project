package ru.practicum.android.diploma.presentation.search

data class SearchUiState(
    val query: String,
) {
    val action: SearchAction = if (query.isEmpty()) SearchAction.SEARCH else SearchAction.CLEAR
}

enum class SearchAction {
    SEARCH,
    CLEAR,
}

package ru.practicum.android.diploma.presentation.search

interface SearchContract {

    interface View {
        fun render(state: SearchUiState)
        fun requestSearchFocus()
        fun openFilters()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onQueryChanged(query: String)
        fun onSearchActionClicked()
        fun onFilterClicked()
    }
}

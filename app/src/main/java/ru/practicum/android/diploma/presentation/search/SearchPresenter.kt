package ru.practicum.android.diploma.presentation.search

import ru.practicum.android.diploma.domain.search.SearchModel

class SearchPresenter(
    private val model: SearchModel,
) : SearchContract.Presenter {

    private var view: SearchContract.View? = null

    override fun attach(view: SearchContract.View) {
        this.view = view
        render()
    }

    override fun detach() {
        view = null
    }

    override fun onQueryChanged(query: String) {
        if (model.query == query) return

        model.query = query
        render()
    }

    override fun onSearchActionClicked() {
        if (model.query.isNotEmpty()) {
            model.query = ""
            render()
        }
        view?.requestSearchFocus()
    }

    override fun onFilterClicked() {
        view?.openFilters()
    }

    private fun render() {
        view?.render(SearchUiState(query = model.query))
    }
}

package ru.practicum.android.diploma.presentation.details

import ru.practicum.android.diploma.domain.details.DetailsModel

class DetailsPresenter(
    private val model: DetailsModel,
) : DetailsContract.Presenter {

    private var view: DetailsContract.View? = null
    private var state = DetailsUiState()

    override fun attach(view: DetailsContract.View) {
        this.view = view
        loadVacancy()
        view.render(state)
    }

    override fun detach() {
        view = null
    }

    override fun onBackPressed() {
        view?.navigateBack()
    }

    override fun onShareClicked() {
        view?.shareVacancy(state.vacancy)
    }

    override fun onFavoriteClicked() {
        val newFavoriteState = model.toggleFavorite()
        state = state.copy(isFavorite = newFavoriteState)
        view?.render(state)
    }

    private fun loadVacancy() {
        val vacancy = model.getVacancy()
        state = DetailsUiState(
            vacancy = vacancy,
            isFavorite = model.isFavorite
        )
        view?.render(state)
    }
}

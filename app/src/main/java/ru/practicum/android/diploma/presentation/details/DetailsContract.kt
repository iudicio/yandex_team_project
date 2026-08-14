package ru.practicum.android.diploma.presentation.details

import ru.practicum.android.diploma.ui.details.VacancyDetailsMock

interface DetailsContract {

    interface View {
        fun render(state: DetailsUiState)
        fun navigateBack()
        fun shareVacancy(vacancy: VacancyDetailsMock)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onBackPressed()
        fun onShareClicked()
        fun onFavoriteClicked()
    }
}

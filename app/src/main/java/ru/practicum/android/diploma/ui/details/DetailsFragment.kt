package ru.practicum.android.diploma.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.InMemoryDetailsModel
import ru.practicum.android.diploma.presentation.details.DetailsContract
import ru.practicum.android.diploma.presentation.details.DetailsPresenter
import ru.practicum.android.diploma.presentation.details.DetailsUiState
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class DetailsFragment : Fragment(), DetailsContract.View {

    private var presenter: DetailsContract.Presenter? = null
    private val state = mutableStateOf(DetailsUiState())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val vacancyId = arguments?.getString(ARG_VACANCY_ID) ?: ""

        val detailsPresenter = DetailsPresenter(
            model = InMemoryDetailsModel(
                vacancyId = vacancyId,
                isFavorite = savedInstanceState?.getBoolean(STATE_IS_FAVORITE) ?: false,
            ),
        )
        presenter = detailsPresenter
        detailsPresenter.attach(this)

        return ComposeView(requireContext()).apply {
            id = R.id.details_compose_view
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DiplomaTheme {
                    DetailsScreen(
                        vacancy = state.value.vacancy,
                        onBackPressed = detailsPresenter::onBackPressed,
                        onShareClicked = detailsPresenter::onShareClicked,
                        onFavoriteClicked = detailsPresenter::onFavoriteClicked,
                        isFavorite = state.value.isFavorite,
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_IS_FAVORITE, state.value.isFavorite)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        super.onDestroyView()
    }

    override fun render(state: DetailsUiState) {
        this.state.value = state
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun shareVacancy(vacancy: VacancyDetailsMock) {
        val shareText = buildString {
            append(vacancy.title)
            append("\n")
            append(vacancy.salary)
            append("\n")
            append(vacancy.employerName)
            append("\n")
            append(vacancy.city)
            append("\n\n")
            append(vacancy.description)
        }

        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(android.content.Intent.createChooser(sendIntent, getString(R.string.share_vacancy)))
    }

    private companion object {
        private const val ARG_VACANCY_ID = "vacancy_id"
        private const val STATE_IS_FAVORITE = "is_favorite"

    }
}

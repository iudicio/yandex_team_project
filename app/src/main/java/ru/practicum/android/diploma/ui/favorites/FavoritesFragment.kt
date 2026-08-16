package ru.practicum.android.diploma.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.di.appContainer
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.ui.details.DetailsFragment
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(requireContext().appContainer.favoritesInteractor)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            id = ru.practicum.android.diploma.R.id.favorites_compose_view
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DiplomaTheme {
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    FavoritesScreen(
                        state = state,
                        onVacancyClick = ::openVacancy,
                    )
                }
            }
        }
    }

    private fun openVacancy(id: String) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.navigationFavorites) {
            val bundle = Bundle().apply {
                putString(DetailsFragment.ARG_VACANCY_ID, id)
            }
            navController.navigate(R.id.detailsFragment, bundle)
        }
    }

    private class FavoritesViewModelFactory(
        private val interactor: FavoritesInteractor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == FavoritesViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }
            return modelClass.cast(FavoritesViewModel(interactor))
        }
    }
}

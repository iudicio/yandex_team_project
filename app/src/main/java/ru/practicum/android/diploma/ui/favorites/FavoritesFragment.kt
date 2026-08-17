package ru.practicum.android.diploma.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.ui.details.DetailsFragment
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            id = R.id.favorites_compose_view
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
}

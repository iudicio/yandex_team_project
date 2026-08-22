package ru.practicum.android.diploma.ui.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.vacancyArguments

class FavoritesFragment : ComposeDestinationFragment() {
    private val viewModel: FavoritesViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        FavoritesScreen(
            state = state,
            onVacancyClick = { vacancyId ->
                if (navController.currentDestination?.id == R.id.navigationFavorites) {
                    navController.navigate(
                        R.id.action_favorites_to_details,
                        vacancyArguments(vacancyId),
                    )
                }
            },
        )
    }
}

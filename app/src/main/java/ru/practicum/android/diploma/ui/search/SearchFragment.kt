package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.presentation.search.SearchEvent
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.vacancyArguments

class SearchFragment : ComposeDestinationFragment() {

    private val viewModel: SearchViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchScreen(
            state = state,
            onQueryChanged = viewModel::onQueryChanged,
            onSearchSubmitted = viewModel::submit,
            onFilterClicked = {
                if (navController.currentDestination?.id == R.id.navigationSearch) {
                    navController.navigate(R.id.action_search_to_filter)
                }
            },
            onLoadNextPage = viewModel::loadNextPage,
            onVacancyClicked = { vacancyId ->
                if (navController.currentDestination?.id == R.id.navigationSearch) {
                    navController.navigate(
                        R.id.action_search_to_details,
                        vacancyArguments(vacancyId),
                    )
                }
            },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    val message = when (event) {
                        is SearchEvent.PagingFailed -> when (event.error) {
                            SearchError.NoInternet -> R.string.search_paging_no_internet
                            SearchError.Generic -> R.string.search_paging_generic_error
                        }
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

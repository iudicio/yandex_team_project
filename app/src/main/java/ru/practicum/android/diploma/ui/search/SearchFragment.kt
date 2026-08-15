package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.di.appContainer
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchEvent
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import ru.practicum.android.diploma.ui.details.DetailsFragment
import ru.practicum.android.diploma.ui.filter.FilterFragment
import ru.practicum.android.diploma.ui.theme.DiplomaTheme
import java.io.IOException

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels {
        val container = requireContext().appContainer
        SearchViewModelFactory(
            vacancyRepository = container.vacancyRepository,
            filterSettingsRepository = container.filterSettingsRepository,
        )
    }
    private val focusRequestKey = mutableIntStateOf(0)
    private val hasActiveFilters = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.search_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                SearchScreen(
                    state = state,
                    focusRequestKey = focusRequestKey.intValue,
                    onQueryChanged = viewModel::onQueryChanged,
                    onSearchActionClicked = {
                        if (state.query.isEmpty()) {
                            focusRequestKey.intValue += 1
                        } else {
                            viewModel.onQueryChanged("")
                        }
                    },
                    onSearchSubmitted = viewModel::submit,
                    onFilterClicked = ::openFilters,
                    onLoadNextPage = viewModel::loadNextPage,
                    hasActiveFilters = hasActiveFilters.value,
                    onVacancyClicked = ::openVacancy
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAppliedFilters()
        observeSearchEvents()
    }

    override fun onResume() {
        super.onResume()
        refreshActiveFilters()
    }

    private fun observeAppliedFilters() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle.getLiveData<Boolean>(FilterFragment.FILTERS_APPLIED_RESULT_KEY)
            .observe(viewLifecycleOwner) { applied ->
                if (applied) {
                    refreshActiveFilters()
                    viewModel.onFiltersApplied()
                }
                handle.remove<Boolean>(FilterFragment.FILTERS_APPLIED_RESULT_KEY)
            }
    }

    private fun observeSearchEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    val message = when (event) {
                        is SearchEvent.PagingFailed -> if (event.cause is IOException) {
                            R.string.search_paging_no_internet
                        } else {
                            R.string.search_paging_error
                        }
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshActiveFilters() {
        hasActiveFilters.value = requireContext()
            .appContainer
            .filterSettingsRepository
            .load()
            .hasActiveFilters
    }

    private fun openFilters() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.navigationHome) {
            navController.navigate(R.id.action_navigationHome_to_navigationFilter)
        }
    }
    private fun openVacancy(id: String) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.navigationHome) {
            val bundle = Bundle().apply {
                putString(DetailsFragment.ARG_VACANCY_ID, id)
            }
            navController.navigate(R.id.detailsFragment, bundle)
        }
    }
}

private class SearchViewModelFactory(
    private val vacancyRepository: VacancyRepository,
    private val filterSettingsRepository: FilterSettingsRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == SearchViewModel::class.java) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return modelClass.cast(
            SearchViewModel(
                savedStateHandle = extras.createSavedStateHandle(),
                vacancyRepository = vacancyRepository,
                filterSettingsRepository = filterSettingsRepository,
            ),
        )
    }
}

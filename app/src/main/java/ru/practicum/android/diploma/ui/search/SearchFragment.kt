package ru.practicum.android.diploma.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.filter.FILTER_SETTINGS_PREFERENCES_NAME
import ru.practicum.android.diploma.data.filter.SharedPreferencesFilterSettingsRepository
import ru.practicum.android.diploma.domain.search.InMemorySearchModel
import ru.practicum.android.diploma.presentation.search.SearchContract
import ru.practicum.android.diploma.presentation.search.SearchPresenter
import ru.practicum.android.diploma.presentation.search.SearchUiState
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class SearchFragment : Fragment(), SearchContract.View {

    private var presenter: SearchContract.Presenter? = null
    private val state = mutableStateOf(SearchUiState(query = ""))
    private val focusRequestKey = mutableIntStateOf(0)
    private val hasActiveFilters = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val searchPresenter = SearchPresenter(
            model = InMemorySearchModel(
                query = savedInstanceState?.getString(STATE_QUERY) ?: state.value.query,
            ),
        )
        presenter = searchPresenter
        searchPresenter.attach(this)

        return ComposeView(requireContext()).apply {
            id = R.id.search_compose_view
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DiplomaTheme {
                    SearchScreen(
                        state = state.value,
                        focusRequestKey = focusRequestKey.intValue,
                        onQueryChanged = searchPresenter::onQueryChanged,
                        onSearchActionClicked = searchPresenter::onSearchActionClicked,
                        onFilterClicked = searchPresenter::onFilterClicked,
                        hasActiveFilters = hasActiveFilters.value,
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_QUERY, state.value.query)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val repository = SharedPreferencesFilterSettingsRepository(
            requireContext().getSharedPreferences(
                FILTER_SETTINGS_PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            ),
        )
        hasActiveFilters.value = repository.load().hasActiveFilters
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        super.onDestroyView()
    }

    override fun render(state: SearchUiState) {
        this.state.value = state
    }

    override fun requestSearchFocus() {
        focusRequestKey.intValue += 1
    }

    override fun openFilters() {
        findNavController().navigate(R.id.action_navigationHome_to_navigationFilter)
    }

    private companion object {
        const val STATE_QUERY = "search_query"
    }
}

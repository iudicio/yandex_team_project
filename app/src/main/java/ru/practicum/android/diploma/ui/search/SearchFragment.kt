package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.search.InMemorySearchModel
import ru.practicum.android.diploma.presentation.search.SearchContract
import ru.practicum.android.diploma.presentation.search.SearchPresenter
import ru.practicum.android.diploma.presentation.search.SearchUiState
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class SearchFragment : Fragment(), SearchContract.View {

    private var presenter: SearchContract.Presenter? = null
    private val state = mutableStateOf(SearchUiState(query = ""))
    private val focusRequestKey = mutableIntStateOf(0)

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
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_QUERY, state.value.query)
        super.onSaveInstanceState(outState)
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
        Toast.makeText(requireContext(), R.string.filters_are_in_development, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val STATE_QUERY = "search_query"
    }
}

package ru.practicum.android.diploma.ui.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.domain.search.InMemorySearchModel
import ru.practicum.android.diploma.presentation.search.SearchAction
import ru.practicum.android.diploma.presentation.search.SearchContract
import ru.practicum.android.diploma.presentation.search.SearchPresenter
import ru.practicum.android.diploma.presentation.search.SearchUiState

class SearchFragment : Fragment(R.layout.fragment_search), SearchContract.View {

    private var binding: FragmentSearchBinding? = null
    private var presenter: SearchContract.Presenter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentBinding = FragmentSearchBinding.bind(view)
        binding = fragmentBinding

        val searchPresenter = SearchPresenter(
            model = InMemorySearchModel(
                query = savedInstanceState?.getString(STATE_QUERY).orEmpty(),
            ),
        )
        presenter = searchPresenter

        fragmentBinding.searchEditText.doAfterTextChanged { editable ->
            searchPresenter.onQueryChanged(editable?.toString().orEmpty())
        }
        fragmentBinding.searchActionButton.setOnClickListener {
            searchPresenter.onSearchActionClicked()
        }
        fragmentBinding.filterButton.setOnClickListener {
            searchPresenter.onFilterClicked()
        }

        searchPresenter.attach(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding?.searchEditText?.text?.toString()?.let { query ->
            outState.putString(STATE_QUERY, query)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        binding = null
        super.onDestroyView()
    }

    override fun render(state: SearchUiState) {
        val fragmentBinding = binding ?: return
        if (fragmentBinding.searchEditText.text.toString() != state.query) {
            fragmentBinding.searchEditText.setText(state.query)
            fragmentBinding.searchEditText.setSelection(state.query.length)
        }

        val (icon, description) = when (state.action) {
            SearchAction.SEARCH -> R.drawable.ic_search to R.string.search_action_description
            SearchAction.CLEAR -> R.drawable.ic_close to R.string.clear_search_description
        }
        fragmentBinding.searchActionButton.setImageResource(icon)
        fragmentBinding.searchActionButton.contentDescription = getString(description)
    }

    override fun requestSearchFocus() {
        val searchEditText = binding?.searchEditText ?: return
        searchEditText.requestFocus()
        searchEditText.post {
            val inputMethodManager = searchEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
            inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun openFilters() {
        Toast.makeText(requireContext(), R.string.filters_are_in_development, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val STATE_QUERY = "search_query"
    }
}

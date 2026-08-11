package ru.practicum.android.diploma.presentation.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.practicum.android.diploma.domain.search.InMemorySearchModel

class SearchPresenterTest {

    private lateinit var model: InMemorySearchModel
    private lateinit var view: FakeSearchView
    private lateinit var presenter: SearchPresenter

    @Before
    fun setUp() {
        model = InMemorySearchModel()
        view = FakeSearchView()
        presenter = SearchPresenter(model)
    }

    @Test
    fun `attach renders initial state`() {
        presenter.attach(view)

        assertEquals(SearchUiState(query = ""), view.lastState)
        assertEquals(SearchAction.SEARCH, view.lastState?.action)
    }

    @Test
    fun `query change is stored in model and renders clear action`() {
        presenter.attach(view)

        presenter.onQueryChanged("Android developer")

        assertEquals("Android developer", model.query)
        assertEquals(SearchAction.CLEAR, view.lastState?.action)
    }

    @Test
    fun `action click clears non-empty query and focuses input`() {
        model.query = "Android"
        presenter.attach(view)

        presenter.onSearchActionClicked()

        assertEquals("", model.query)
        assertEquals(SearchAction.SEARCH, view.lastState?.action)
        assertTrue(view.focusRequested)
    }

    @Test
    fun `filter click delegates navigation to view`() {
        presenter.attach(view)

        presenter.onFilterClicked()

        assertTrue(view.filtersOpened)
    }

    @Test
    fun `detached presenter no longer calls view`() {
        presenter.attach(view)
        presenter.detach()
        view.reset()

        presenter.onFilterClicked()
        presenter.onSearchActionClicked()

        assertFalse(view.filtersOpened)
        assertFalse(view.focusRequested)
        assertEquals(null, view.lastState)
    }

    private class FakeSearchView : SearchContract.View {
        var lastState: SearchUiState? = null
        var focusRequested = false
        var filtersOpened = false

        override fun render(state: SearchUiState) {
            lastState = state
        }

        override fun requestSearchFocus() {
            focusRequested = true
        }

        override fun openFilters() {
            filtersOpened = true
        }

        fun reset() {
            lastState = null
            focusRequested = false
            filtersOpened = false
        }
    }
}

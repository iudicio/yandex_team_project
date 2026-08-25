package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelInputTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `blank query stays initial and never searches`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository()
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("   ")
        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(repository.requests.isEmpty())
        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)
    }

    @Test
    fun `nonblank query searches after exactly two seconds`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        advanceTimeBy(DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(repository.requests.isEmpty())

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("Android"), repository.requests.map { request -> request.text })
        assertTrue(viewModel.state.value.result is SearchResultUiState.Content)
    }

    @Test
    fun `new input cancels previous debounce`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("And")
        advanceTimeBy(DEBOUNCE_MILLIS / 2)
        viewModel.onQueryChanged("Android")
        advanceTimeBy(DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(repository.requests.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("Android"), repository.requests.map { request -> request.text })
    }

    @Test
    fun `submit cancels debounce and searches immediately once`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        assertEquals(SearchResultUiState.Loading, viewModel.state.value.result)
        runCurrent()
        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `clear cancels pending search and saves empty query`() = runTest(mainDispatcherRule.dispatcher) {
        val handle = SavedStateHandle()
        val repository = QueueVacancyRepository()
        val viewModel = createViewModel(repository, handle)

        viewModel.onQueryChanged("Android")
        viewModel.onQueryChanged("")
        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(repository.requests.isEmpty())
        assertEquals("", handle.get<String>(SearchViewModel.QUERY_KEY))
        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)
    }

    @Test
    fun `restored query starts one debounced search`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        createViewModel(
            repository,
            SavedStateHandle(mapOf(SearchViewModel.QUERY_KEY to "Android")),
        )

        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(1, repository.requests.size)
    }

    private fun createViewModel(
        repository: QueueVacancyRepository,
        handle: SavedStateHandle = SavedStateHandle(),
    ): SearchViewModel = SearchViewModel(
        savedStateHandle = handle,
        searchInteractor = SearchVacanciesInteractorImpl(repository),
        filterSettingsInteractor = FakeFilterSettingsInteractor(),
    )

    private companion object {
        const val DEBOUNCE_MILLIS = 2_000L
    }
}

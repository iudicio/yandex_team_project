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
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.ManualVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.MutableFilterRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.QueueVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.result
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.vacancy

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelInputTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `restored blank query stays initial and does not search`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository()
        val viewModel = createViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf(SearchViewModel.QUERY_KEY to "   ")),
        )

        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals("   ", viewModel.state.value.query)
        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `restored nonblank query searches after exact debounce`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result(items = listOf(vacancy("restored")))))
        }
        val viewModel = createViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf(SearchViewModel.QUERY_KEY to "Android")),
        )

        advanceTimeBy(DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(repository.requests.isEmpty())
        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("Android"), repository.requests.map { request -> request.text })
        assertTrue(viewModel.state.value.result is SearchResultUiState.Content)
    }

    @Test
    fun `new input cancels previous debounce`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result()))
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
    fun `submit cancels debounce and searches immediately`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result()))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()

        assertEquals(SearchResultUiState.Loading, viewModel.state.value.result)
        runCurrent()
        assertEquals(1, repository.requests.size)

        advanceTimeBy(DEBOUNCE_MILLIS)
        runCurrent()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `clearing query cancels stale response and persists query`() = runTest(mainDispatcherRule.dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val repository = ManualVacancyRepository()
        val viewModel = createViewModel(repository, savedStateHandle = savedStateHandle)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()
        viewModel.onQueryChanged("")

        assertEquals("", savedStateHandle.get<String>(SearchViewModel.QUERY_KEY))
        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)

        repository.complete("Android", page = 1, Result.success(result()))
        runCurrent()

        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)
    }

    private fun createViewModel(
        repository: VacancyRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): SearchViewModel = SearchViewModel(
        savedStateHandle = savedStateHandle,
        searchInteractor = SearchVacanciesInteractorImpl(repository),
        filterSettingsRepository = MutableFilterRepository(),
        dispatcher = mainDispatcherRule.dispatcher,
    )

    private companion object {
        const val DEBOUNCE_MILLIS = 2_000L
    }
}

package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelPagingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `one page result never requests another page`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page(page = 1, pages = 1)))
        }
        val viewModel = createViewModel(repository)
        searchNow(viewModel)

        viewModel.loadNextPage()
        runCurrent()

        assertEquals(listOf(1), repository.requests.map { request -> request.page })
    }

    @Test
    fun `next page is requested once concurrently and successful page is not repeated`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(SearchOutcome.Success(page(page = 1, pages = 2, items = listOf(vacancy("1")))))
                enqueue(SearchOutcome.Success(page(page = 2, pages = 2, items = listOf(vacancy("2")))))
            }
            val viewModel = createViewModel(repository)
            searchNow(viewModel)

            viewModel.loadNextPage()
            viewModel.loadNextPage()
            assertTrue((viewModel.state.value.result as SearchResultUiState.Content).isLoadingNextPage)
            runCurrent()
            viewModel.loadNextPage()
            runCurrent()

            assertEquals(listOf(1, 2), repository.requests.map { request -> request.page })
        }

    @Test
    fun `next page removes duplicate ids and appends new vacancies`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(SearchOutcome.Success(page(page = 1, pages = 2, items = listOf(vacancy("1")))))
                enqueue(
                    SearchOutcome.Success(
                        page(page = 2, pages = 2, items = listOf(vacancy("1"), vacancy("2"))),
                    ),
                )
            }
            val viewModel = createViewModel(repository)
            searchNow(viewModel)

            viewModel.loadNextPage()
            runCurrent()

            val content = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(listOf("1", "2"), content.items.map { item -> item.id })
            assertFalse(content.isLoadingNextPage)
        }

    @Test
    fun `duplicate-only page advances pagination trigger without adding rows`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(SearchOutcome.Success(page(page = 1, pages = 3, items = listOf(vacancy("1")))))
                enqueue(SearchOutcome.Success(page(page = 2, pages = 3, items = listOf(vacancy("1")))))
                enqueue(SearchOutcome.Success(page(page = 3, pages = 3, items = listOf(vacancy("2")))))
            }
            val viewModel = createViewModel(repository)
            searchNow(viewModel)

            viewModel.loadNextPage()
            runCurrent()

            val content = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(listOf("1"), content.items.map { item -> item.id })
            assertEquals(2, content.lastLoadedPage)
            assertFalse(content.isLoadingNextPage)

            viewModel.loadNextPage()
            runCurrent()

            assertEquals(listOf(1, 2, 3), repository.requests.map { request -> request.page })
            assertEquals(
                listOf("1", "2"),
                (viewModel.state.value.result as SearchResultUiState.Content).items.map { item -> item.id },
            )
        }

    @Test
    fun `paging failure preserves list hides loader emits event and permits retry`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(SearchOutcome.Success(page(page = 1, pages = 2, items = listOf(vacancy("1")))))
                enqueue(SearchOutcome.Failure(SearchError.NoInternet))
                enqueue(SearchOutcome.Success(page(page = 2, pages = 2, items = listOf(vacancy("2")))))
            }
            val events = mutableListOf<SearchEvent>()
            val viewModel = createViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.collect { event -> events += event }
            }
            searchNow(viewModel)

            viewModel.loadNextPage()
            runCurrent()

            val failed = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(listOf("1"), failed.items.map { item -> item.id })
            assertFalse(failed.isLoadingNextPage)
            assertEquals(SearchEvent.PagingFailed(SearchError.NoInternet), events.single())

            viewModel.loadNextPage()
            runCurrent()
            val recovered = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(listOf("1", "2"), recovered.items.map { item -> item.id })
            assertEquals(listOf(1, 2, 2), repository.requests.map { request -> request.page })
        }

    @Test
    fun `generic paging failure emits generic event`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page(page = 1, pages = 2)))
            enqueue(SearchOutcome.Failure(SearchError.Generic))
        }
        val events = mutableListOf<SearchEvent>()
        val viewModel = createViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { event -> events += event }
        }
        searchNow(viewModel)

        viewModel.loadNextPage()
        runCurrent()

        assertEquals(SearchEvent.PagingFailed(SearchError.Generic), events.single())
    }

    @Test
    fun `paging failure waits until a stopped screen resumes collection`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(SearchOutcome.Success(page(page = 1, pages = 2)))
                enqueue(SearchOutcome.Failure(SearchError.NoInternet))
            }
            val viewModel = createViewModel(repository)
            searchNow(viewModel)

            viewModel.loadNextPage()
            runCurrent()

            assertEquals(
                SearchEvent.PagingFailed(SearchError.NoInternet),
                viewModel.events.first(),
            )
        }

    private fun searchNow(viewModel: SearchViewModel) {
        viewModel.onQueryChanged("Android")
        viewModel.submit()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
    }

    private fun createViewModel(repository: QueueVacancyRepository): SearchViewModel = SearchViewModel(
        savedStateHandle = SavedStateHandle(),
        searchInteractor = SearchVacanciesInteractorImpl(repository),
    )
}

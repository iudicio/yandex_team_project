package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.ManualVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.MutableFilterRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.QueueVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.result
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.vacancy
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelPagingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `next page deduplicates ids and stops at last page`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result(page = 1, pages = 2, found = 3, items = listOf(vacancy("1")))))
            enqueue(
                Result.success(
                    result(
                        page = 2,
                        pages = 2,
                        found = 3,
                        items = listOf(vacancy("1"), vacancy("2")),
                    ),
                ),
            )
        }
        val viewModel = createViewModel(repository)
        startSearch(viewModel)

        viewModel.loadNextPage()
        assertTrue((viewModel.state.value.result as SearchResultUiState.Content).isLoadingNextPage)
        viewModel.loadNextPage()
        runCurrent()

        val content = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals(listOf("1", "2"), content.items.map { vacancy -> vacancy.id })
        assertTrue(!content.isLoadingNextPage)
        assertEquals(listOf(1, 2), repository.requests.map { request -> request.page })

        viewModel.loadNextPage()
        runCurrent()
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun `paging failure preserves content emits once and allows retry`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = IOException("paging offline")
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result(page = 1, pages = 2, items = listOf(vacancy("1")))))
            enqueue(Result.failure(expected))
            enqueue(Result.success(result(page = 2, pages = 2, items = listOf(vacancy("2")))))
        }
        val events = mutableListOf<SearchEvent>()
        val viewModel = createViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { event -> events += event }
        }
        startSearch(viewModel)

        viewModel.loadNextPage()
        runCurrent()

        val failedContent = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals(listOf("1"), failedContent.items.map { vacancy -> vacancy.id })
        assertTrue(!failedContent.isLoadingNextPage)
        assertEquals(1, events.size)
        assertSame(expected, (events.single() as SearchEvent.PagingFailed).cause)

        viewModel.loadNextPage()
        runCurrent()
        val retriedContent = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals(listOf("1", "2"), retriedContent.items.map { vacancy -> vacancy.id })
        assertEquals(1, events.size)
    }

    @Test
    fun `filter changes do not affect paging until apply restarts page one`() =
        runTest(mainDispatcherRule.dispatcher) {
            val filters = MutableFilterRepository(FilterSettings(countryId = 1))
            val repository = QueueVacancyRepository().apply {
                enqueue(Result.success(result(page = 1, pages = 2, items = listOf(vacancy("1")))))
                enqueue(Result.success(result(page = 2, pages = 2, items = listOf(vacancy("2")))))
                enqueue(Result.success(result(page = 1, pages = 1, items = listOf(vacancy("new")))))
            }
            val viewModel = createViewModel(repository, filters)
            startSearch(viewModel)

            filters.settings = FilterSettings(regionId = 10, industryId = "7", onlyWithSalary = true)
            viewModel.loadNextPage()
            runCurrent()

            assertEquals(1, repository.requests[1].area)
            assertEquals(2, repository.requests[1].page)

            viewModel.onFiltersApplied()
            assertEquals(SearchResultUiState.Loading, viewModel.state.value.result)
            runCurrent()

            assertEquals(10, repository.requests[2].area)
            assertEquals(7, repository.requests[2].industry)
            assertEquals(true, repository.requests[2].onlyWithSalary)
            assertEquals(1, repository.requests[2].page)
            val content = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(listOf("new"), content.items.map { vacancy -> vacancy.id })
        }

    @Test
    fun `late paging response cannot replace a new query generation`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = ManualVacancyRepository()
        val viewModel = SearchViewModel(
            savedStateHandle = SavedStateHandle(),
            vacancyRepository = repository,
            filterSettingsRepository = MutableFilterRepository(),
            dispatcher = mainDispatcherRule.dispatcher,
            debounceMillis = SearchViewModel.DEFAULT_DEBOUNCE_MILLIS,
        )
        viewModel.onQueryChanged("Old")
        viewModel.submit()
        runCurrent()
        repository.complete(
            query = "Old",
            page = 1,
            result = Result.success(result(page = 1, pages = 2, items = listOf(vacancy("old-1")))),
        )
        runCurrent()
        viewModel.loadNextPage()
        runCurrent()

        viewModel.onQueryChanged("New")
        viewModel.submit()
        runCurrent()
        repository.complete(
            query = "New",
            page = 1,
            result = Result.success(result(items = listOf(vacancy("new")))),
        )
        runCurrent()
        repository.complete(
            query = "Old",
            page = 2,
            result = Result.success(result(page = 2, pages = 2, items = listOf(vacancy("old-2")))),
        )
        runCurrent()

        val content = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals("New", viewModel.state.value.query)
        assertEquals(listOf("new"), content.items.map { vacancy -> vacancy.id })
    }

    private fun startSearch(viewModel: SearchViewModel) {
        viewModel.onQueryChanged("Android")
        viewModel.submit()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
    }

    private fun createViewModel(
        repository: QueueVacancyRepository,
        filters: MutableFilterRepository = MutableFilterRepository(),
    ): SearchViewModel = SearchViewModel(
        savedStateHandle = SavedStateHandle(),
        vacancyRepository = repository,
        filterSettingsRepository = filters,
        dispatcher = mainDispatcherRule.dispatcher,
        debounceMillis = SearchViewModel.DEFAULT_DEBOUNCE_MILLIS,
    )
}

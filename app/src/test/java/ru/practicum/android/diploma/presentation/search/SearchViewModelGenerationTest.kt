package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import ru.practicum.android.diploma.domain.search.VacancySearchResult
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.ManualVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.MutableFilterRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.QueueVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.result
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.vacancy
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelGenerationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `late old response cannot replace newer generation`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = ManualVacancyRepository()
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Old")
        viewModel.submit()
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
            page = 1,
            result = Result.success(result(items = listOf(vacancy("old")))),
        )
        runCurrent()

        val content = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals("New", viewModel.state.value.query)
        assertEquals(listOf("new"), content.items.map { vacancy -> vacancy.id })
    }

    @Test
    fun `apply with blank query stays initial without request`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository()
        val viewModel = createViewModel(repository)

        viewModel.onFiltersApplied()
        runCurrent()

        assertEquals(SearchResultUiState.Initial, viewModel.state.value.result)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `apply cancels pending request and immediately uses fresh filters`() = runTest(mainDispatcherRule.dispatcher) {
        val filters = MutableFilterRepository(FilterSettings(countryId = 1))
        val repository = ManualVacancyRepository()
        val viewModel = createViewModel(repository, filters)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()
        filters.settings = FilterSettings(regionId = 10)
        viewModel.onFiltersApplied()
        runCurrent()

        assertEquals(listOf(1, 10), repository.requests.map { request -> request.area })
        repository.complete(
            query = "Android",
            page = 1,
            result = Result.success(result(items = listOf(vacancy("old")))),
        )
        runCurrent()
        assertEquals(SearchResultUiState.Loading, viewModel.state.value.result)
        repository.complete(
            query = "Android",
            page = 1,
            result = Result.success(result(items = listOf(vacancy("fresh")))),
        )
        runCurrent()

        val content = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals(listOf("fresh"), content.items.map { vacancy -> vacancy.id })
    }

    @Test
    fun `debounced search keeps filter snapshot taken when query changed`() = runTest(mainDispatcherRule.dispatcher) {
        val filters = MutableFilterRepository(FilterSettings(countryId = 1))
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result()))
        }
        val viewModel = createViewModel(repository, filters)

        viewModel.onQueryChanged("Android")
        filters.settings = FilterSettings(regionId = 10)
        advanceTimeBy(SearchViewModel.DEFAULT_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(1, repository.requests.single().area)
    }

    @Test
    fun `exception thrown by repository becomes no internet state`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = IOException("offline")
        val viewModel = createViewModel(ThrowingVacancyRepository(expected))

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        val state = viewModel.state.value.result as SearchResultUiState.NoInternet
        assertEquals(expected, state.cause)
    }

    @Test
    fun `view model exposes no back action`() {
        assertFalse(
            SearchViewModel::class.java.declaredMethods.any { method ->
                method.name.contains("back", ignoreCase = true)
            },
        )
    }

    private fun createViewModel(
        repository: VacancyRepository,
        filters: MutableFilterRepository = MutableFilterRepository(),
    ): SearchViewModel = SearchViewModel(
        savedStateHandle = SavedStateHandle(),
        vacancyRepository = repository,
        filterSettingsRepository = filters,
        dispatcher = mainDispatcherRule.dispatcher,
        debounceMillis = SearchViewModel.DEFAULT_DEBOUNCE_MILLIS,
    )
}

private class ThrowingVacancyRepository(
    private val throwable: Throwable,
) : VacancyRepository {
    override suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult> = throw throwable
}

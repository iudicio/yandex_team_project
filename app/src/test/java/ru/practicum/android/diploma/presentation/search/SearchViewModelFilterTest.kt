package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelFilterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saved filters update icon without restarting last search`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        val filters = FakeFilterSettingsInteractor()
        val viewModel = createViewModel(repository, filters)
        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()
        repository.requests.clear()

        filters.save(FilterSettings(salary = 100_000))
        runCurrent()

        assertTrue(viewModel.state.value.hasActiveFilters)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `clearing query keeps active filter indicator`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
        }
        val filters = FakeFilterSettingsInteractor(FilterSettings(salary = 100_000))
        val viewModel = createViewModel(repository, filters)
        viewModel.onQueryChanged("Android")
        runCurrent()

        viewModel.onQueryChanged("")
        runCurrent()

        assertTrue(viewModel.state.value.hasActiveFilters)
        assertTrue(viewModel.state.value.query.isEmpty())
    }

    @Test
    fun `apply repeats nonblank query with latest filters`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
            enqueue(SearchOutcome.Success(page()))
        }
        val filters = FakeFilterSettingsInteractor()
        val viewModel = createViewModel(repository, filters)
        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        filters.save(
            FilterSettings(
                salary = 120_000,
                onlyWithSalary = true,
                industry = Industry("7.540", "IT"),
                country = Area(113, "Россия"),
                region = Area(1, "Москва"),
            ),
        )
        viewModel.onFiltersApplied()
        runCurrent()

        assertEquals(2, repository.requests.size)
        assertEquals(120_000, repository.requests.last().salary)
        assertTrue(repository.requests.last().onlyWithSalary)
        assertEquals("7.540", repository.requests.last().industryId)
        assertEquals(1, repository.requests.last().areaId)
        assertEquals(filters.current(), filters.applied())
    }

    @Test
    fun `applying reset repeats query without filter parameters`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page()))
            enqueue(SearchOutcome.Success(page()))
        }
        val filters = FakeFilterSettingsInteractor(
            FilterSettings(
                salary = 120_000,
                onlyWithSalary = true,
                industry = Industry("7.540", "IT"),
                country = Area(113, "Россия"),
                region = Area(1, "Москва"),
            ),
        )
        val viewModel = createViewModel(repository, filters)
        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        filters.reset()
        viewModel.onFiltersApplied()
        runCurrent()

        assertEquals(2, repository.requests.size)
        assertNull(repository.requests.last().salary)
        assertFalse(repository.requests.last().onlyWithSalary)
        assertNull(repository.requests.last().industryId)
        assertNull(repository.requests.last().areaId)
        assertEquals(filters.current(), filters.applied())
    }

    private fun createViewModel(
        repository: QueueVacancyRepository,
        filters: FakeFilterSettingsInteractor,
    ): SearchViewModel = SearchViewModel(
        savedStateHandle = SavedStateHandle(),
        searchInteractor = SearchVacanciesInteractorImpl(repository),
        filterSettingsInteractor = filters,
        debounceMillis = 0L,
    )
}

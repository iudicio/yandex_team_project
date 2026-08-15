package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.MutableFilterRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.QueueVacancyRepository
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.result
import ru.practicum.android.diploma.presentation.search.SearchViewModelTestFixture.vacancy
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelResultTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `first page removes duplicate ids and exposes content`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(
                Result.success(
                    result(
                        found = 3,
                        items = listOf(vacancy("1"), vacancy("1"), vacancy("2")),
                    ),
                ),
            )
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        val content = viewModel.state.value.result as SearchResultUiState.Content
        assertEquals(3, content.found)
        assertEquals(listOf("1", "2"), content.items.map { vacancy -> vacancy.id })
        assertTrue(!content.isLoadingNextPage)
    }

    @Test
    fun `successful response without valid cards exposes empty`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.success(result(found = 0, items = emptyList())))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        assertEquals(SearchResultUiState.Empty, viewModel.state.value.result)
    }

    @Test
    fun `io failure exposes no internet`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = IOException("offline")
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.failure(expected))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        val state = viewModel.state.value.result as SearchResultUiState.NoInternet
        assertSame(expected, state.cause)
    }

    @Test
    fun `non-io failure exposes error`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = IllegalStateException("server error")
        val repository = QueueVacancyRepository().apply {
            enqueue(Result.failure(expected))
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChanged("Android")
        viewModel.submit()
        runCurrent()

        val state = viewModel.state.value.result as SearchResultUiState.Error
        assertSame(expected, state.cause)
    }

    private fun createViewModel(repository: QueueVacancyRepository): SearchViewModel = SearchViewModel(
        savedStateHandle = SavedStateHandle(),
        searchInteractor = SearchVacanciesInteractorImpl(repository),
        filterSettingsRepository = MutableFilterRepository(),
        dispatcher = mainDispatcherRule.dispatcher,
        debounceMillis = SearchViewModel.DEFAULT_DEBOUNCE_MILLIS,
    )
}

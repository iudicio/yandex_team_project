package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelResultTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful result deduplicates first page and keeps found count`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = QueueVacancyRepository().apply {
                enqueue(
                    SearchOutcome.Success(
                        page(found = 10, items = listOf(vacancy("1"), vacancy("1"), vacancy("2"))),
                    ),
                )
            }
            val viewModel = createViewModel(repository)

            searchNow(viewModel)

            val content = viewModel.state.value.result as SearchResultUiState.Content
            assertEquals(10, content.found)
            assertEquals(listOf("1", "2"), content.items.map { item -> item.id })
        }

    @Test
    fun `successful page without valid items becomes empty`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Success(page(found = 0, items = emptyList())))
        }
        val viewModel = createViewModel(repository)

        searchNow(viewModel)

        assertEquals(SearchResultUiState.Empty, viewModel.state.value.result)
    }

    @Test
    fun `no internet has dedicated first page state`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Failure(SearchError.NoInternet))
        }
        val viewModel = createViewModel(repository)

        searchNow(viewModel)

        assertEquals(SearchResultUiState.NoInternet, viewModel.state.value.result)
    }

    @Test
    fun `other failures use generic first page state`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = QueueVacancyRepository().apply {
            enqueue(SearchOutcome.Failure(SearchError.Generic))
        }
        val viewModel = createViewModel(repository)

        searchNow(viewModel)

        assertEquals(SearchResultUiState.GenericError, viewModel.state.value.result)
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

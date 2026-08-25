package ru.practicum.android.diploma.presentation.filter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.IndustriesInteractor
import ru.practicum.android.diploma.domain.filter.Industry

@OptIn(ExperimentalCoroutinesApi::class)
class IndustryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `canonicalizes selection and searches locally ignoring case`() = runTest(
        mainDispatcherRule.dispatcher,
    ) {
        val saved = Industry("7.540", "Old name")
        val actual = Industry("7.540", "Разработка программного обеспечения")
        val viewModel = IndustryViewModel(
            FakeIndustriesInteractor(FilterCatalogOutcome.Success(listOf(Industry("1", "Авто"), actual))),
            FakeFilterSettingsInteractor(FilterSettings(industry = saved)),
        )

        advanceUntilIdle()
        assertEquals(actual, viewModel.state.value.selectedIndustry)
        assertTrue(viewModel.state.value.canConfirm)

        viewModel.onQueryChanged("ПРОГРАММ")
        assertEquals(
            listOf(actual),
            (viewModel.state.value.result as IndustryResultUiState.Content).items,
        )

        viewModel.onQueryChanged("нет совпадений")
        assertEquals(IndustryResultUiState.Empty, viewModel.state.value.result)
        assertTrue(viewModel.state.value.canConfirm)
    }

    @Test
    fun `single selection is saved only on confirm`() = runTest(mainDispatcherRule.dispatcher) {
        val first = Industry("1", "Авто")
        val second = Industry("7.540", "IT")
        val settings = FakeFilterSettingsInteractor()
        val viewModel = IndustryViewModel(
            FakeIndustriesInteractor(FilterCatalogOutcome.Success(listOf(first, second))),
            settings,
        )
        advanceUntilIdle()

        viewModel.select(first)
        viewModel.select(second)
        assertTrue(settings.saved.isEmpty())

        val event = async { viewModel.events.first() }
        viewModel.confirm()

        assertEquals(second, settings.current().industry)
        assertEquals(IndustryEvent.Confirmed(second), event.await())
    }

    @Test
    fun `network error is dedicated and retry can recover`() = runTest(mainDispatcherRule.dispatcher) {
        val saved = Industry("1", "Авто")
        val interactor = QueueIndustriesInteractor(
            FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet),
            FilterCatalogOutcome.Success(listOf(saved)),
        )
        val viewModel = IndustryViewModel(
            interactor,
            FakeFilterSettingsInteractor(FilterSettings(industry = saved)),
        )

        advanceUntilIdle()
        assertEquals(IndustryResultUiState.NoInternet, viewModel.state.value.result)
        assertFalse(viewModel.state.value.canConfirm)

        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.result is IndustryResultUiState.Content)
        assertTrue(viewModel.state.value.canConfirm)
        assertEquals(2, interactor.calls)
    }

    @Test
    fun `stale saved industry cannot be confirmed when absent from catalog`() = runTest(
        mainDispatcherRule.dispatcher,
    ) {
        val saved = Industry("stale", "Устаревшая отрасль")
        val viewModel = IndustryViewModel(
            FakeIndustriesInteractor(
                FilterCatalogOutcome.Success(listOf(Industry("1", "Авто"))),
            ),
            FakeFilterSettingsInteractor(FilterSettings(industry = saved)),
        )

        advanceUntilIdle()

        assertTrue(viewModel.state.value.result is IndustryResultUiState.Content)
        assertFalse(viewModel.state.value.canConfirm)
    }
}

private class FakeIndustriesInteractor(
    private val outcome: FilterCatalogOutcome<List<Industry>>,
) : IndustriesInteractor {
    override suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>> = outcome
}

private class QueueIndustriesInteractor(
    vararg outcomes: FilterCatalogOutcome<List<Industry>>,
) : IndustriesInteractor {
    private val outcomes = ArrayDeque(outcomes.toList())
    var calls = 0

    override suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>> {
        calls += 1
        return outcomes.removeFirst()
    }
}

package ru.practicum.android.diploma.presentation.filter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.toSelection

@OptIn(ExperimentalCoroutinesApi::class)
class CountryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads root countries`() = runTest(mainDispatcherRule.dispatcher) {
        val (interactor, _) = areasInteractor(FilterCatalogOutcome.Success(areaTree()))
        val viewModel = CountryViewModel(interactor, FakeFilterSettingsInteractor())

        advanceUntilIdle()
        assertEquals(
            listOf(40, 113),
            (viewModel.state.value.result as AreaResultUiState.Content).items.map(Area::id),
        )
    }

    @Test
    fun `selecting another country saves it and clears incompatible region`() = runTest(mainDispatcherRule.dispatcher) {
        val current = FilterSettings(country = Area(113, "Россия"), region = Area(1, "Москва", 113))
        val settings = FakeFilterSettingsInteractor(current)
        val (interactor, _) = areasInteractor(FilterCatalogOutcome.Success(areaTree()))
        val viewModel = CountryViewModel(interactor, settings)
        advanceUntilIdle()
        val kazakhstan = (viewModel.state.value.result as AreaResultUiState.Content).items.first()
        val event = async { viewModel.events.first() }

        viewModel.select(kazakhstan)

        assertEquals(40, settings.current().country?.id)
        assertEquals(null, settings.current().region)
        assertEquals(CountryEvent.Selected(kazakhstan.toSelection()), event.await())
    }

    @Test
    fun `dedicated error state retries`() = runTest(mainDispatcherRule.dispatcher) {
        val (interactor, repository) = areasInteractor(
            FilterCatalogOutcome.Failure(FilterCatalogError.Generic),
            FilterCatalogOutcome.Success(areaTree()),
        )
        val viewModel = CountryViewModel(interactor, FakeFilterSettingsInteractor())
        advanceUntilIdle()
        assertEquals(AreaResultUiState.GenericError, viewModel.state.value.result)

        assertEquals(1, repository.calls)

        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.result is AreaResultUiState.Content)
        assertEquals(2, repository.calls)
    }
}

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
class RegionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `country restricts regions and local query searches substring`() = runTest(mainDispatcherRule.dispatcher) {
        val settings = FakeFilterSettingsInteractor(FilterSettings(country = Area(40, "Казахстан")))
        val (interactor, _) = areasInteractor(FilterCatalogOutcome.Success(areaTree()))
        val viewModel = RegionViewModel(interactor, settings)

        advanceUntilIdle()
        assertEquals(
            listOf(160),
            (viewModel.state.value.result as AreaResultUiState.Content).items.map(Area::id),
        )

        viewModel.onQueryChanged("МАТ")
        assertEquals(
            listOf(160),
            (viewModel.state.value.result as AreaResultUiState.Content).items.map(Area::id),
        )

        viewModel.onQueryChanged("Москва")
        assertEquals(AreaResultUiState.Empty, viewModel.state.value.result)
    }

    @Test
    fun `selecting deep region automatically saves its root country`() = runTest(mainDispatcherRule.dispatcher) {
        val settings = FakeFilterSettingsInteractor()
        val (interactor, _) = areasInteractor(FilterCatalogOutcome.Success(areaTree()))
        val viewModel = RegionViewModel(interactor, settings)
        advanceUntilIdle()
        val centralDistrict = (viewModel.state.value.result as AreaResultUiState.Content)
            .items.first { area -> area.id == 101 }
        val event = async { viewModel.events.first() }

        viewModel.select(centralDistrict)

        assertEquals(113, settings.current().country?.id)
        assertEquals(101, settings.current().region?.id)
        assertEquals(101, settings.current().areaId)
        assertEquals(RegionEvent.Selected(centralDistrict.toSelection()), event.await())
    }

    @Test
    fun `no internet and empty catalog remain distinct`() = runTest(mainDispatcherRule.dispatcher) {
        val (offlineInteractor, _) = areasInteractor(FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet))
        val offline = RegionViewModel(offlineInteractor, FakeFilterSettingsInteractor())
        advanceUntilIdle()
        assertEquals(AreaResultUiState.NoInternet, offline.state.value.result)

        val (emptyInteractor, _) = areasInteractor(FilterCatalogOutcome.Success(emptyList()))
        val empty = RegionViewModel(emptyInteractor, FakeFilterSettingsInteractor())
        advanceUntilIdle()
        assertEquals(AreaResultUiState.Empty, empty.state.value.result)
        assertTrue(empty.state.value.query.isEmpty())
    }
}

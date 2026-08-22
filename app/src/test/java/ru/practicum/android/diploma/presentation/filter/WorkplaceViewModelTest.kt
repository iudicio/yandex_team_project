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
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings

@OptIn(ExperimentalCoroutinesApi::class)
class WorkplaceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `observes settings and clears region independently`() = runTest(mainDispatcherRule.dispatcher) {
        val country = Area(113, "Россия")
        val region = Area(1, "Москва", 113)
        val settings = FakeFilterSettingsInteractor(FilterSettings(country = country, region = region))
        val viewModel = WorkplaceViewModel(settings)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.hasSelection)

        viewModel.clearRegion()
        advanceUntilIdle()

        assertEquals(country, viewModel.state.value.country)
        assertEquals(null, viewModel.state.value.region)
        assertTrue(viewModel.state.value.hasSelection)

        viewModel.clearCountry()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.hasSelection)
    }

    @Test
    fun `confirm returns auto saved selection and back is separate`() = runTest(mainDispatcherRule.dispatcher) {
        val country = Area(113, "Россия")
        val region = Area(1, "Москва", 113)
        val viewModel = WorkplaceViewModel(
            FakeFilterSettingsInteractor(FilterSettings(country = country, region = region)),
        )
        val confirmed = async { viewModel.events.first() }

        viewModel.confirm()
        assertEquals(WorkplaceEvent.Confirmed(country, region), confirmed.await())

        val closed = async { viewModel.events.first() }
        viewModel.back()
        assertEquals(WorkplaceEvent.Close, closed.await())
    }
}

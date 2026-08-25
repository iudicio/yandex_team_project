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
import ru.practicum.android.diploma.domain.filter.Industry

@OptIn(ExperimentalCoroutinesApi::class)
class FilterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `valid edits save immediately and make filters active`() = runTest(
        mainDispatcherRule.dispatcher,
    ) {
        val interactor = FakeFilterSettingsInteractor()
        val viewModel = FilterViewModel(interactor)

        viewModel.onSalaryChanged("120000")
        viewModel.onOnlyWithSalaryChanged(true)
        viewModel.onIndustrySelected(Industry("7.540", "Разработка ПО"))
        viewModel.onWorkplaceSelected(Area(113, "Россия"), Area(1, "Москва", 113))
        advanceUntilIdle()

        assertEquals(120_000, interactor.current().salary)
        assertTrue(interactor.current().onlyWithSalary)
        assertEquals("7.540", interactor.current().industryId)
        assertEquals(1, interactor.current().areaId)
        assertEquals("120000", viewModel.state.value.salaryInput)
        assertTrue(viewModel.state.value.hasActiveFilters)
        assertTrue(viewModel.state.value.canApply)
        assertEquals(4, interactor.saved.size)
    }

    @Test
    fun `salary draft keeps multiple leading zeroes`() = runTest(mainDispatcherRule.dispatcher) {
        val interactor = FakeFilterSettingsInteractor()
        val viewModel = FilterViewModel(interactor)

        viewModel.onSalaryChanged("000")
        advanceUntilIdle()

        assertEquals(0, interactor.current().salary)
        assertEquals("000", viewModel.state.value.salaryInput)
        assertTrue(viewModel.state.value.canApply)
    }

    @Test
    fun `non numeric and overflowing salary input is ignored without crash`() = runTest(mainDispatcherRule.dispatcher) {
        val interactor = FakeFilterSettingsInteractor(FilterSettings(salary = 50_000))
        val viewModel = FilterViewModel(interactor)

        viewModel.onSalaryChanged("50 001")
        viewModel.onSalaryChanged("2147483648")
        advanceUntilIdle()

        assertEquals(50_000, interactor.current().salary)
        assertEquals("50000", viewModel.state.value.salaryInput)
        assertTrue(interactor.saved.isEmpty())

        viewModel.onSalaryChanged("")
        advanceUntilIdle()
        assertEquals(null, interactor.current().salary)
    }

    @Test
    fun `reset clears settings and conditional actions`() = runTest(mainDispatcherRule.dispatcher) {
        val interactor = FakeFilterSettingsInteractor(
            FilterSettings(salary = 90_000, onlyWithSalary = true, industry = Industry("1", "Авто")),
        )
        val viewModel = FilterViewModel(interactor)

        viewModel.reset()
        advanceUntilIdle()

        assertEquals(FilterSettings(), interactor.current())
        assertEquals(1, interactor.resetCount)
        assertFalse(viewModel.state.value.hasActiveFilters)
        assertTrue(viewModel.state.value.canApply)
    }

    @Test
    fun `apply carries current settings while back only closes`() = runTest(mainDispatcherRule.dispatcher) {
        val settings = FilterSettings(salary = 100_000)
        val interactor = FakeFilterSettingsInteractor(settings)
        val viewModel = FilterViewModel(interactor)
        val applied = async { viewModel.events.first() }

        viewModel.apply()
        assertEquals(FilterEvent.Applied(settings), applied.await())
        assertEquals(settings, interactor.applied())
        assertFalse(viewModel.state.value.canApply)

        val closed = async { viewModel.events.first() }
        viewModel.back()
        assertEquals(FilterEvent.Close, closed.await())
    }
}

package ru.practicum.android.diploma.domain.filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterSettingsInteractorTest {
    @Test
    fun `current save observe and reset delegate to repository`() {
        val repository = RecordingSettingsRepository(FilterSettings(salary = 10_000))
        val interactor = FilterSettingsInteractorImpl(repository)
        val updated = FilterSettings(onlyWithSalary = true, industry = Industry("7.540", "IT"))

        assertEquals(10_000, interactor.current().salary)
        assertEquals(repository.flow, interactor.observe())
        assertEquals(repository.flow.value, interactor.applied())

        interactor.save(updated)
        assertEquals(updated, interactor.current())
        assertEquals(10_000, interactor.applied().salary)

        interactor.markApplied(updated)
        assertEquals(updated, interactor.applied())

        interactor.reset()
        assertEquals(FilterSettings(), interactor.current())
    }
}

private class RecordingSettingsRepository(initial: FilterSettings) : FilterSettingsRepository {
    val flow = MutableStateFlow(initial)

    override fun observe(): StateFlow<FilterSettings> = flow

    override fun save(settings: FilterSettings) {
        flow.value = settings
    }

    override fun reset() {
        flow.value = FilterSettings()
    }
}

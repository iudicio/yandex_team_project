package ru.practicum.android.diploma.presentation.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository

class FilterViewModelTest {

    @Test
    fun `initial state is restored without saving`() {
        val restoredSettings = FilterSettings(
            salary = "00100",
            industryId = "7",
            industryName = "IT",
            countryId = 1,
            countryName = "Россия",
            regionId = 10,
            regionName = "Москва",
            onlyWithSalary = true,
        )
        val repository = FakeFilterSettingsRepository(restoredSettings)

        val viewModel = FilterViewModel(repository)

        assertEquals("00100", viewModel.state.value.salary)
        assertEquals("7", viewModel.state.value.industryId)
        assertEquals("IT", viewModel.state.value.industryName)
        assertEquals(1, viewModel.state.value.countryId)
        assertEquals("Россия", viewModel.state.value.countryName)
        assertEquals(10, viewModel.state.value.regionId)
        assertEquals("Москва", viewModel.state.value.regionName)
        assertEquals("Россия, Москва", viewModel.state.value.workplaceName)
        assertTrue(viewModel.state.value.onlyWithSalary)
        assertTrue(viewModel.state.value.hasActiveFilters)
        assertEquals(0, repository.savedSettings.size)
    }

    @Test
    fun `salary keeps digits and leading zeros then saves immediately`() {
        val repository = FakeFilterSettingsRepository()
        val viewModel = FilterViewModel(repository)

        viewModel.onSalaryChanged("00a12 3.4")

        assertEquals("001234", viewModel.state.value.salary)
        assertEquals("001234", repository.savedSettings.single().salary)
        assertTrue(viewModel.state.value.hasActiveFilters)
    }

    @Test
    fun `same normalized salary does not save twice`() {
        val repository = FakeFilterSettingsRepository()
        val viewModel = FilterViewModel(repository)

        viewModel.onSalaryChanged("12")
        viewModel.onSalaryChanged("1a2")

        assertEquals(1, repository.savedSettings.size)
    }

    @Test
    fun `salary flag changes are saved but repeated value is ignored`() {
        val repository = FakeFilterSettingsRepository()
        val viewModel = FilterViewModel(repository)

        viewModel.onOnlyWithSalaryChanged(true)
        viewModel.onOnlyWithSalaryChanged(true)
        viewModel.onOnlyWithSalaryChanged(false)

        assertFalse(viewModel.state.value.onlyWithSalary)
        assertFalse(viewModel.state.value.hasActiveFilters)
        assertEquals(2, repository.savedSettings.size)
        assertTrue(repository.savedSettings.first().onlyWithSalary)
        assertFalse(repository.savedSettings.last().onlyWithSalary)
    }

    @Test
    fun `industry selection and clearing save both nullable fields`() {
        val repository = FakeFilterSettingsRepository()
        val viewModel = FilterViewModel(repository)

        viewModel.onIndustrySelected(industryId = "9", industryName = "Разработка ПО")

        assertEquals("9", viewModel.state.value.industryId)
        assertEquals("Разработка ПО", viewModel.state.value.industryName)
        assertTrue(viewModel.state.value.hasActiveFilters)

        viewModel.onIndustryCleared()

        assertNull(viewModel.state.value.industryId)
        assertNull(viewModel.state.value.industryName)
        assertFalse(viewModel.state.value.hasActiveFilters)
        assertEquals(2, repository.savedSettings.size)
    }

    @Test
    fun `workplace selection and clearing save country and region together`() {
        val repository = FakeFilterSettingsRepository()
        val viewModel = FilterViewModel(repository)

        viewModel.onWorkplaceSelected(
            countryId = 1,
            countryName = "Россия",
            regionId = 10,
            regionName = "Москва",
        )

        assertEquals(1, viewModel.state.value.countryId)
        assertEquals(10, viewModel.state.value.regionId)
        assertEquals("Россия, Москва", viewModel.state.value.workplaceName)
        assertEquals(10, repository.savedSettings.single().areaId)

        viewModel.onWorkplaceCleared()

        assertNull(viewModel.state.value.countryId)
        assertNull(viewModel.state.value.countryName)
        assertNull(viewModel.state.value.regionId)
        assertNull(viewModel.state.value.regionName)
        assertNull(viewModel.state.value.workplaceName)
        assertEquals(2, repository.savedSettings.size)
    }

    @Test
    fun `reset clears all settings and repeated reset is ignored`() {
        val repository = FakeFilterSettingsRepository(
            FilterSettings(
                salary = "100000",
                industryId = "1",
                industryName = "Информационные технологии",
                countryId = 1,
                countryName = "Россия",
                regionId = 10,
                regionName = "Москва",
                onlyWithSalary = true,
            ),
        )
        val viewModel = FilterViewModel(repository)

        viewModel.onResetClicked()
        viewModel.onResetClicked()

        assertEquals(FilterUiState(), viewModel.state.value)
        assertEquals(FilterSettings(), repository.savedSettings.single())
    }

    private class FakeFilterSettingsRepository(
        private val initialSettings: FilterSettings = FilterSettings(),
    ) : FilterSettingsRepository {
        val savedSettings = mutableListOf<FilterSettings>()

        override fun load(): FilterSettings = initialSettings

        override fun save(settings: FilterSettings) {
            savedSettings += settings
        }
    }
}

package ru.practicum.android.diploma.domain.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterSettingsTest {

    @Test
    fun `region has priority over country as search area`() {
        val settings = FilterSettings(countryId = 1, regionId = 10)

        assertEquals(10, settings.areaId)
        assertTrue(settings.hasActiveFilters)
    }

    @Test
    fun `country is search area when region is absent`() {
        val settings = FilterSettings(countryId = 1)

        assertEquals(1, settings.areaId)
        assertTrue(settings.hasActiveFilters)
    }

    @Test
    fun `empty settings have no search area and are inactive`() {
        val settings = FilterSettings()

        assertEquals(null, settings.areaId)
        assertFalse(settings.hasActiveFilters)
    }

    @Test
    fun `orphan display names are not active filter parameters`() {
        val settings = FilterSettings(
            industryName = "IT",
            countryName = "Россия",
            regionName = "Москва",
        )

        assertEquals(null, settings.areaId)
        assertFalse(settings.hasActiveFilters)
    }
}

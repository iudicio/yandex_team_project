package ru.practicum.android.diploma.data.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.toSelection

class SharedPreferencesFilterSettingsRepositoryTest {
    @Test
    fun `restores legacy string values without unsafe casts`() {
        val preferences = InMemorySharedPreferences(
            mapOf(
                "filter_salary" to "120000",
                "filter_only_with_salary" to "true",
                "filter_industry_id" to "7.540",
                "filter_industry_name" to "  Разработка ПО  ",
                "filter_country_id" to "113",
                "filter_country_name" to "Россия",
                "filter_region_id" to 1L,
                "filter_region_name" to "Москва",
            ),
        )

        val restored = SharedPreferencesFilterSettingsRepository(preferences).observe().value

        assertEquals(120_000, restored.salary)
        assertTrue(restored.onlyWithSalary)
        assertEquals(Industry("7.540", "Разработка ПО"), restored.industry)
        assertEquals(113, restored.country?.id)
        assertEquals(1, restored.region?.id)
        assertEquals(1, restored.areaId)
    }

    @Test
    fun `save is observable and survives repository recreation`() {
        val preferences = InMemorySharedPreferences()
        val repository = SharedPreferencesFilterSettingsRepository(preferences)
        val settings = FilterSettings(
            salary = 90_000,
            onlyWithSalary = true,
            industry = Industry("9.399", "Образование"),
            country = Area(113, "Россия", areas = listOf(Area(1, "Москва"))),
            region = Area(1, "Москва", parentId = 113, areas = listOf(Area(2, "ЦАО"))),
        )

        repository.save(settings)

        val current = repository.observe().value
        val expected = settings.copy(
            country = settings.country?.toSelection(),
            region = settings.region?.toSelection(),
        )
        assertEquals(expected, current)
        assertTrue(current.country?.areas.isNullOrEmpty())
        assertEquals(current, SharedPreferencesFilterSettingsRepository(preferences).observe().value)
    }

    @Test
    fun `invalid and overflowing salary values are safely omitted`() {
        listOf("not-a-number", "2147483648", -1L).forEach { rawSalary ->
            val repository = SharedPreferencesFilterSettingsRepository(
                InMemorySharedPreferences(mapOf("filter_salary" to rawSalary)),
            )

            assertNull(repository.observe().value.salary)
        }
    }

    @Test
    fun `reset removes filter keys but preserves unrelated preferences`() {
        val preferences = InMemorySharedPreferences(mapOf("unrelated" to "keep"))
        val repository = SharedPreferencesFilterSettingsRepository(preferences)
        repository.save(FilterSettings(salary = 50_000, onlyWithSalary = true))

        repository.reset()

        assertEquals(FilterSettings(), repository.observe().value)
        assertEquals("keep", preferences.snapshot["unrelated"])
        assertFalse(preferences.snapshot.keys.any { key -> key.startsWith("filter_") })
    }
}

package ru.practicum.android.diploma.data.filter

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterSettings

class SharedPreferencesFilterSettingsRepositoryTest {

    @Test
    fun `all filter settings survive repository recreation`() {
        val preferences = InMemorySharedPreferences()
        val settings = FilterSettings(
            salary = "00100000",
            industryId = "7",
            industryName = "IT",
            countryId = 1,
            countryName = "Россия",
            regionId = 10,
            regionName = "Москва",
            onlyWithSalary = true,
        )

        SharedPreferencesFilterSettingsRepository(preferences).save(settings)
        val restored = SharedPreferencesFilterSettingsRepository(preferences).load()

        assertEquals(settings, restored)
        assertEquals(10, restored.areaId)
    }

    @Test
    fun `saving reset state removes nullable selections`() {
        val preferences = InMemorySharedPreferences()
        val repository = SharedPreferencesFilterSettingsRepository(preferences)
        repository.save(
            FilterSettings(
                industryId = "7",
                industryName = "IT",
                countryId = 1,
                countryName = "Россия",
                regionId = 10,
                regionName = "Москва",
            ),
        )

        repository.save(FilterSettings())

        assertEquals(FilterSettings(), repository.load())
        assertFalse(preferences.contains("filter_industry_id"))
        assertFalse(preferences.contains("filter_country_id"))
        assertFalse(preferences.contains("filter_region_id"))
    }

    @Test
    fun `zero area ids remain values rather than becoming null`() {
        val preferences = InMemorySharedPreferences()
        val repository = SharedPreferencesFilterSettingsRepository(preferences)

        repository.save(FilterSettings(countryId = 0, regionId = 0))

        assertTrue(preferences.contains("filter_country_id"))
        assertEquals(0, repository.load().countryId)
        assertEquals(0, repository.load().regionId)
    }

    @Test
    fun `display names without ids are neither stored nor restored`() {
        val preferences = InMemorySharedPreferences()
        val repository = SharedPreferencesFilterSettingsRepository(preferences)

        repository.save(
            FilterSettings(
                industryName = "IT",
                countryName = "Россия",
                regionName = "Москва",
            ),
        )

        assertEquals(FilterSettings(), repository.load())
        assertFalse(preferences.contains("filter_industry_name"))
        assertFalse(preferences.contains("filter_country_name"))
        assertFalse(preferences.contains("filter_region_name"))

        preferences.edit()
            .putString("filter_industry_name", "Legacy IT")
            .putString("filter_country_name", "Legacy country")
            .putString("filter_region_name", "Legacy region")
            .apply()

        assertEquals(FilterSettings(), repository.load())
    }

    private class InMemorySharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values.toMutableMap()

        override fun getString(key: String, defaultValue: String?): String? =
            values[key] as? String ?: defaultValue

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String, defaultValues: Set<String>?): Set<String>? =
            values[key] as? Set<String> ?: defaultValues

        override fun getInt(key: String, defaultValue: Int): Int = values[key] as? Int ?: defaultValue

        override fun getLong(key: String, defaultValue: Long): Long = values[key] as? Long ?: defaultValue

        override fun getFloat(key: String, defaultValue: Float): Float = values[key] as? Float ?: defaultValue

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key] as? Boolean ?: defaultValue

        override fun contains(key: String): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor(values)

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) = Unit
    }

    private class Editor(
        private val values: MutableMap<String, Any?>,
    ) : SharedPreferences.Editor {
        private val updates = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var clearRequested = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = update(key, value)

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor = update(key, values)

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = update(key, value)

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = update(key, value)

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = update(key, value)

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = update(key, value)

        override fun remove(key: String): SharedPreferences.Editor = apply {
            removals += key
            updates -= key
        }

        override fun clear(): SharedPreferences.Editor = apply {
            clearRequested = true
            updates.clear()
            removals.clear()
        }

        override fun commit(): Boolean {
            persist()
            return true
        }

        override fun apply() = persist()

        private fun update(key: String, value: Any?): SharedPreferences.Editor = apply {
            updates[key] = value
            removals -= key
        }

        private fun persist() {
            if (clearRequested) {
                values.clear()
            }
            removals.forEach(values::remove)
            values.putAll(updates)
        }
    }
}

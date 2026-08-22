package ru.practicum.android.diploma.data.filter

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.filter.Industry

class SharedPreferencesFilterSettingsRepository(
    private val preferences: SharedPreferences,
) : FilterSettingsRepository {
    private val mutableSettings = MutableStateFlow(readSettings())
    private val settings = mutableSettings.asStateFlow()

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in FILTER_KEYS) mutableSettings.value = readSettings()
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun observe(): StateFlow<FilterSettings> = settings

    override fun save(settings: FilterSettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putNullableInt(KEY_SALARY, normalized.salary)
            .putBoolean(KEY_ONLY_WITH_SALARY, normalized.onlyWithSalary)
            .putNullableString(KEY_INDUSTRY_ID, normalized.industry?.id)
            .putNullableString(KEY_INDUSTRY_NAME, normalized.industry?.name)
            .putNullableInt(KEY_COUNTRY_ID, normalized.country?.id)
            .putNullableString(KEY_COUNTRY_NAME, normalized.country?.name)
            .putNullableInt(KEY_REGION_ID, normalized.region?.id)
            .putNullableString(KEY_REGION_NAME, normalized.region?.name)
            .apply()
        mutableSettings.value = normalized
    }

    override fun reset() {
        preferences.edit().also { editor ->
            FILTER_KEYS.forEach(editor::remove)
        }.apply()
        mutableSettings.value = FilterSettings()
    }

    private fun readSettings(): FilterSettings {
        val values = preferences.all
        return FilterSettings(
            salary = values.safeInt(KEY_SALARY)?.takeIf { salary -> salary >= 0 },
            onlyWithSalary = values.safeBoolean(KEY_ONLY_WITH_SALARY),
            industry = readIndustry(values),
            country = readArea(values, KEY_COUNTRY_ID, KEY_COUNTRY_NAME),
            region = readArea(values, KEY_REGION_ID, KEY_REGION_NAME),
        ).normalized()
    }

    private companion object {
        const val KEY_SALARY = "filter_salary"
        const val KEY_ONLY_WITH_SALARY = "filter_only_with_salary"
        const val KEY_INDUSTRY_ID = "filter_industry_id"
        const val KEY_INDUSTRY_NAME = "filter_industry_name"
        const val KEY_COUNTRY_ID = "filter_country_id"
        const val KEY_COUNTRY_NAME = "filter_country_name"
        const val KEY_REGION_ID = "filter_region_id"
        const val KEY_REGION_NAME = "filter_region_name"

        val FILTER_KEYS = setOf(
            KEY_SALARY,
            KEY_ONLY_WITH_SALARY,
            KEY_INDUSTRY_ID,
            KEY_INDUSTRY_NAME,
            KEY_COUNTRY_ID,
            KEY_COUNTRY_NAME,
            KEY_REGION_ID,
            KEY_REGION_NAME,
        )
    }
}

private fun readIndustry(values: Map<String, *>): Industry? {
    val id = values.safeString("filter_industry_id")
    val name = values.safeString("filter_industry_name")
    return if (id != null && name != null) Industry(id, name) else null
}

private fun readArea(values: Map<String, *>, idKey: String, nameKey: String): Area? {
    val id = values.safeInt(idKey)
    val name = values.safeString(nameKey)
    return if (id != null && name != null) Area(id = id, name = name) else null
}

private fun FilterSettings.normalized(): FilterSettings = copy(
    salary = salary?.takeIf { value -> value >= 0 },
    industry = industry?.let { value ->
        val id = value.id.trim()
        val name = value.name.trim()
        if (id.isNotEmpty() && name.isNotEmpty()) Industry(id, name) else null
    },
    country = country?.normalizedSelection(),
    region = region?.normalizedSelection(),
)

private fun Area.normalizedSelection(): Area? {
    val validName = name.trim().takeIf(String::isNotEmpty) ?: return null
    return copy(name = validName, parentId = null, areas = emptyList())
}

private fun Map<String, *>.safeString(key: String): String? = get(key)?.toString()?.trim()
    ?.takeIf(String::isNotEmpty)

private fun Map<String, *>.safeInt(key: String): Int? = when (val value = get(key)) {
    is Int -> value
    is Number -> value.toLong().takeIf { number -> number in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
    is String -> value.trim().toIntOrNull()
    else -> null
}

private fun Map<String, *>.safeBoolean(key: String): Boolean = when (val value = get(key)) {
    is Boolean -> value
    is String -> value.toBooleanStrictOrNull() ?: false
    else -> false
}

private fun SharedPreferences.Editor.putNullableString(key: String, value: String?): SharedPreferences.Editor =
    if (value == null) remove(key) else putString(key, value)

private fun SharedPreferences.Editor.putNullableInt(key: String, value: Int?): SharedPreferences.Editor =
    if (value == null) remove(key) else putInt(key, value)

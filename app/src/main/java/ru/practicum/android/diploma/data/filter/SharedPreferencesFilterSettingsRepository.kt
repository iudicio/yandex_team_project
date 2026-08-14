package ru.practicum.android.diploma.data.filter

import android.content.SharedPreferences
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository

const val FILTER_SETTINGS_PREFERENCES_NAME = "filter_settings"

class SharedPreferencesFilterSettingsRepository(
    private val sharedPreferences: SharedPreferences,
) : FilterSettingsRepository {

    override fun load(): FilterSettings {
        val industryId = sharedPreferences.getString(KEY_INDUSTRY_ID, null)
        val countryId = sharedPreferences.getNullableInt(KEY_COUNTRY_ID)
        val regionId = sharedPreferences.getNullableInt(KEY_REGION_ID)
        return FilterSettings(
            salary = sharedPreferences.getString(KEY_SALARY, "").orEmpty(),
            industryId = industryId,
            industryName = sharedPreferences.getString(KEY_INDUSTRY_NAME, null).takeIf { industryId != null },
            countryId = countryId,
            countryName = sharedPreferences.getString(KEY_COUNTRY_NAME, null).takeIf { countryId != null },
            regionId = regionId,
            regionName = sharedPreferences.getString(KEY_REGION_NAME, null).takeIf { regionId != null },
            onlyWithSalary = sharedPreferences.getBoolean(KEY_ONLY_WITH_SALARY, false),
        )
    }

    override fun save(settings: FilterSettings) {
        val editor = sharedPreferences.edit()
            .putString(KEY_SALARY, settings.salary)
            .putBoolean(KEY_ONLY_WITH_SALARY, settings.onlyWithSalary)

        if (settings.industryId == null) {
            editor.remove(KEY_INDUSTRY_ID)
        } else {
            editor.putString(KEY_INDUSTRY_ID, settings.industryId)
        }

        editor.putNullableString(
            KEY_INDUSTRY_NAME,
            settings.industryName.takeIf { settings.industryId != null },
        )

        editor.putNullableInt(KEY_COUNTRY_ID, settings.countryId)
        editor.putNullableString(KEY_COUNTRY_NAME, settings.countryName.takeIf { settings.countryId != null })
        editor.putNullableInt(KEY_REGION_ID, settings.regionId)
        editor.putNullableString(KEY_REGION_NAME, settings.regionName.takeIf { settings.regionId != null })

        editor.apply()
    }

    private companion object {
        const val KEY_SALARY = "filter_salary"
        const val KEY_INDUSTRY_ID = "filter_industry_id"
        const val KEY_INDUSTRY_NAME = "filter_industry_name"
        const val KEY_COUNTRY_ID = "filter_country_id"
        const val KEY_COUNTRY_NAME = "filter_country_name"
        const val KEY_REGION_ID = "filter_region_id"
        const val KEY_REGION_NAME = "filter_region_name"
        const val KEY_ONLY_WITH_SALARY = "filter_only_with_salary"
    }
}

private fun SharedPreferences.getNullableInt(key: String): Int? =
    if (contains(key)) getInt(key, 0) else null

private fun SharedPreferences.Editor.putNullableInt(
    key: String,
    value: Int?,
): SharedPreferences.Editor = if (value == null) remove(key) else putInt(key, value)

private fun SharedPreferences.Editor.putNullableString(
    key: String,
    value: String?,
): SharedPreferences.Editor = if (value == null) remove(key) else putString(key, value)

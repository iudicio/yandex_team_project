package ru.practicum.android.diploma.domain.filter

interface FilterSettingsRepository {
    fun load(): FilterSettings
    fun save(settings: FilterSettings)
}

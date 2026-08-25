package ru.practicum.android.diploma.domain.filter

import kotlinx.coroutines.flow.StateFlow

interface FilterSettingsRepository {
    fun observe(): StateFlow<FilterSettings>
    fun save(settings: FilterSettings)
    fun reset()
}

package ru.practicum.android.diploma.domain.filter

import kotlinx.coroutines.flow.StateFlow

interface FilterSettingsInteractor {
    fun observe(): StateFlow<FilterSettings>
    fun current(): FilterSettings
    fun applied(): FilterSettings
    fun save(settings: FilterSettings)
    fun reset()
    fun markApplied(settings: FilterSettings = current())
}

class FilterSettingsInteractorImpl(
    private val repository: FilterSettingsRepository,
) : FilterSettingsInteractor {
    @Volatile
    private var appliedSettings = repository.observe().value

    override fun observe(): StateFlow<FilterSettings> = repository.observe()

    override fun current(): FilterSettings = repository.observe().value

    override fun applied(): FilterSettings = appliedSettings

    override fun save(settings: FilterSettings) = repository.save(settings)

    override fun reset() = repository.reset()

    override fun markApplied(settings: FilterSettings) {
        appliedSettings = settings
    }
}

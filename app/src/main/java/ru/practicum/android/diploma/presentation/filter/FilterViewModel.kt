package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor
import ru.practicum.android.diploma.domain.filter.Industry

class FilterViewModel(
    private val settingsInteractor: FilterSettingsInteractor,
) : ViewModel() {
    private var salaryInput = settingsInteractor.current().salary?.toString().orEmpty()
    private val mutableState = MutableStateFlow(createState(settingsInteractor.current()))
    val state: StateFlow<FilterUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<FilterEvent>(Channel.BUFFERED)
    val events: Flow<FilterEvent> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observe().collect { settings ->
                if (!salaryInput.represents(settings.salary)) {
                    salaryInput = settings.salary?.toString().orEmpty()
                }
                mutableState.value = createState(settings)
            }
        }
    }

    fun onSalaryChanged(input: String) {
        if (input.any { character -> !character.isDigit() }) return
        val salary = input.takeIf(String::isNotEmpty)?.toIntOrNull()
        if (input.isNotEmpty() && salary == null) return
        salaryInput = input
        val settings = settingsInteractor.current().copy(salary = salary)
        mutableState.value = createState(settings)
        settingsInteractor.save(settings)
    }

    fun onOnlyWithSalaryChanged(checked: Boolean) {
        settingsInteractor.save(settingsInteractor.current().copy(onlyWithSalary = checked))
    }

    fun onIndustrySelected(industry: Industry?) {
        settingsInteractor.save(settingsInteractor.current().copy(industry = industry))
    }

    fun clearIndustry() = onIndustrySelected(null)

    fun onWorkplaceSelected(country: Area?, region: Area?) {
        settingsInteractor.save(settingsInteractor.current().copy(country = country, region = region))
    }

    fun clearWorkplace() {
        settingsInteractor.save(settingsInteractor.current().copy(country = null, region = null))
    }

    fun reset() {
        salaryInput = ""
        settingsInteractor.reset()
        mutableState.value = createState(settingsInteractor.current())
    }

    fun apply() {
        val settings = settingsInteractor.current()
        settingsInteractor.markApplied(settings)
        mutableState.value = createState(settings)
        eventChannel.trySend(FilterEvent.Applied(settings))
    }

    fun back() {
        eventChannel.trySend(FilterEvent.Close)
    }

    private fun createState(settings: FilterSettings): FilterUiState =
        settings.toFilterUiState(
            salaryInput = salaryInput,
            canApply = settings != settingsInteractor.applied(),
        )
}

private fun String.represents(salary: Int?): Boolean = if (isEmpty()) {
    salary == null
} else {
    toIntOrNull() == salary
}

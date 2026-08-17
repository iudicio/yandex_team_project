package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository

class FilterViewModel(
    private val repository: FilterSettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(repository.load().toUiState())
    val state: StateFlow<FilterUiState> = _state.asStateFlow()

    fun onSalaryChanged(salary: String) {
        updateState { currentState ->
            currentState.copy(salary = salary.filter(Char::isDigit))
        }
    }

    fun onOnlyWithSalaryChanged(onlyWithSalary: Boolean) {
        updateState { currentState ->
            currentState.copy(onlyWithSalary = onlyWithSalary)
        }
    }

    fun onWorkplaceSelected(
        countryId: Int?,
        countryName: String?,
        regionId: Int?,
        regionName: String?,
    ) {
        updateState { currentState ->
            currentState.copy(
                countryId = countryId,
                countryName = countryName,
                regionId = regionId,
                regionName = regionName,
            )
        }
    }

    fun onWorkplaceCleared() {
        updateState { currentState ->
            currentState.copy(
                countryId = null,
                countryName = null,
                regionId = null,
                regionName = null,
            )
        }
    }

    fun onIndustrySelected(industryId: String, industryName: String) {
        updateState { currentState ->
            currentState.copy(
                industryId = industryId,
                industryName = industryName,
            )
        }
    }

    fun onIndustryCleared() {
        updateState { currentState ->
            currentState.copy(
                industryId = null,
                industryName = null,
            )
        }
    }

    fun onResetClicked() {
        updateState { FilterUiState() }
    }

    private fun updateState(transform: (FilterUiState) -> FilterUiState) {
        val updatedState = transform(_state.value)
        if (updatedState == _state.value) {
            return
        }

        _state.value = updatedState
        repository.save(updatedState.toSettings())
    }
}

private fun FilterSettings.toUiState(): FilterUiState = FilterUiState(
    salary = salary,
    countryId = countryId,
    countryName = countryName,
    regionId = regionId,
    regionName = regionName,
    industryId = industryId,
    industryName = industryName,
    onlyWithSalary = onlyWithSalary,
)

private fun FilterUiState.toSettings(): FilterSettings = FilterSettings(
    salary = salary,
    countryId = countryId,
    countryName = countryName,
    regionId = regionId,
    regionName = regionName,
    industryId = industryId,
    industryName = industryName,
    onlyWithSalary = onlyWithSalary,
)

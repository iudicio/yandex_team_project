package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.RegionSelection

class WorkplaceViewModel(
    initialSettings: FilterSettings,
) : ViewModel() {

    private val _state = MutableStateFlow(
        WorkplaceUiState(
            countryId = initialSettings.countryId,
            countryName = initialSettings.countryName,
            regionId = initialSettings.regionId,
            regionName = initialSettings.regionName,
        ),
    )
    val state: StateFlow<WorkplaceUiState> = _state.asStateFlow()

    fun onCountrySelected(country: AreaSelection) {
        val previous = _state.value
        val countryChanged = previous.countryId != country.id
        _state.value = previous.copy(
            countryId = country.id,
            countryName = country.name,
            regionId = if (countryChanged) null else previous.regionId,
            regionName = if (countryChanged) null else previous.regionName,
        )
    }

    fun onRegionSelected(region: RegionSelection) {
        _state.value = WorkplaceUiState(
            countryId = region.countryId,
            countryName = region.countryName,
            regionId = region.id,
            regionName = region.name,
        )
    }

    fun onCountryCleared() {
        _state.value = WorkplaceUiState()
    }

    fun onRegionCleared() {
        _state.value = _state.value.copy(regionId = null, regionName = null)
    }
}

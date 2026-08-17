package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.CatalogRepository

class RegionViewModel(
    private val repository: CatalogRepository,
    private val countryId: Int?,
    selectedRegionId: Int?,
) : ViewModel() {

    private val _state = MutableStateFlow(RegionUiState(selectedRegionId = selectedRegionId))
    val state: StateFlow<RegionUiState> = _state.asStateFlow()

    init {
        loadRegions()
    }

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun retry() {
        loadRegions()
    }

    private fun loadRegions() {
        _state.value = _state.value.copy(loadState = CatalogLoadState.LOADING)
        viewModelScope.launch {
            repository.getRegions(countryId).fold(
                onSuccess = { regions ->
                    _state.value = _state.value.copy(
                        loadState = CatalogLoadState.CONTENT,
                        regions = regions,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(loadState = CatalogLoadState.ERROR)
                },
            )
        }
    }
}

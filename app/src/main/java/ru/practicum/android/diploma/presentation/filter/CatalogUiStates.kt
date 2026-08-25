package ru.practicum.android.diploma.presentation.filter

import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry

data class IndustryUiState(
    val query: String = "",
    val result: IndustryResultUiState = IndustryResultUiState.Loading,
    val selectedIndustry: Industry? = null,
    val isSelectionValid: Boolean = false,
) {
    val canConfirm: Boolean
        get() {
            val selectedId = selectedIndustry?.id ?: return false
            val visibleIndustries = (result as? IndustryResultUiState.Content)?.items ?: return false
            return isSelectionValid && visibleIndustries.any { industry -> industry.id == selectedId }
        }
}

sealed interface IndustryResultUiState {
    data object Loading : IndustryResultUiState
    data class Content(val items: List<Industry>) : IndustryResultUiState
    data object Empty : IndustryResultUiState
    data object NoInternet : IndustryResultUiState
    data object ServerError : IndustryResultUiState
    data object GenericError : IndustryResultUiState
}

sealed interface IndustryEvent {
    data class Confirmed(val industry: Industry) : IndustryEvent
    data object Close : IndustryEvent
}

sealed interface AreaResultUiState {
    data object Loading : AreaResultUiState
    data class Content(val items: List<Area>) : AreaResultUiState
    data object Empty : AreaResultUiState
    data object NoInternet : AreaResultUiState
    data object ServerError : AreaResultUiState
    data object GenericError : AreaResultUiState
}

data class CountryUiState(
    val result: AreaResultUiState = AreaResultUiState.Loading,
    val selectedCountryId: Int? = null,
)

sealed interface CountryEvent {
    data class Selected(val country: Area) : CountryEvent
    data object Close : CountryEvent
}

data class RegionUiState(
    val query: String = "",
    val result: AreaResultUiState = AreaResultUiState.Loading,
    val selectedRegionId: Int? = null,
)

sealed interface RegionEvent {
    data class Selected(val region: Area) : RegionEvent
    data object Close : RegionEvent
}

data class WorkplaceUiState(
    val country: Area? = null,
    val region: Area? = null,
) {
    val hasSelection: Boolean
        get() = region != null || country != null
}

sealed interface WorkplaceEvent {
    data class Confirmed(val country: Area?, val region: Area?) : WorkplaceEvent
    data object Close : WorkplaceEvent
}

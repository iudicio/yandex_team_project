package ru.practicum.android.diploma.presentation.filter

import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.RegionSelection

enum class CatalogLoadState {
    LOADING,
    CONTENT,
    ERROR,
}

data class CountryUiState(
    val loadState: CatalogLoadState = CatalogLoadState.LOADING,
    val countries: List<AreaSelection> = emptyList(),
    val selectedCountryId: Int? = null,
)

data class RegionUiState(
    val loadState: CatalogLoadState = CatalogLoadState.LOADING,
    val query: String = "",
    val regions: List<RegionSelection> = emptyList(),
    val selectedRegionId: Int? = null,
) {
    val visibleRegions: List<RegionSelection>
        get() = if (loadState != CatalogLoadState.CONTENT) {
            emptyList()
        } else if (query.isBlank()) {
            regions
        } else {
            regions.filter { region -> region.name.contains(query.trim(), ignoreCase = true) }
        }
}

data class IndustryUiState(
    val loadState: CatalogLoadState = CatalogLoadState.LOADING,
    val query: String = "",
    val industries: List<Industry> = emptyList(),
    val selectedIndustryId: String? = null,
) {
    val visibleIndustries: List<Industry>
        get() = if (loadState != CatalogLoadState.CONTENT) {
            emptyList()
        } else if (query.isBlank()) {
            industries
        } else {
            industries.filter { industry -> industry.name.contains(query.trim(), ignoreCase = true) }
        }

    val selectedIndustry: Industry?
        get() = industries.firstOrNull { industry -> industry.id == selectedIndustryId }
}

data class WorkplaceUiState(
    val countryId: Int? = null,
    val countryName: String? = null,
    val regionId: Int? = null,
    val regionName: String? = null,
) {
    val canChoose: Boolean
        get() = countryId != null || regionId != null
}

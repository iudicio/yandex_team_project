package ru.practicum.android.diploma.ui.filter

import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.presentation.filter.AreaResultUiState
import ru.practicum.android.diploma.presentation.filter.CountryUiState
import ru.practicum.android.diploma.presentation.filter.FilterUiState
import ru.practicum.android.diploma.presentation.filter.IndustryResultUiState
import ru.practicum.android.diploma.presentation.filter.IndustryUiState
import ru.practicum.android.diploma.presentation.filter.RegionUiState
import ru.practicum.android.diploma.presentation.filter.WorkplaceUiState

internal fun FilterUiState.filterScreenModel(): FilterScreenModel = FilterScreenModel(
    salaryInput = salaryInput,
    onlyWithSalary = onlyWithSalary,
    industryName = industry?.name,
    workplaceName = workplaceName(country = country, region = region),
    hasActiveFilters = hasActiveFilters,
    canApply = canApply,
)

internal fun IndustryUiState.industryScreenModel(): IndustryScreenModel = IndustryScreenModel(
    query = query,
    content = result.catalogContent(selectedIndustry?.id),
    hasSelection = canConfirm,
)

internal fun WorkplaceUiState.workplaceScreenModel(): WorkplaceScreenModel = WorkplaceScreenModel(
    countryName = country?.name,
    regionName = region?.name,
    hasSelection = hasSelection,
)

internal fun CountryUiState.countryScreenModel(): CountryScreenModel = CountryScreenModel(
    content = result.catalogContent(selectedCountryId),
)

internal fun RegionUiState.regionScreenModel(): RegionScreenModel = RegionScreenModel(
    query = query,
    content = result.catalogContent(selectedRegionId),
)

private fun IndustryResultUiState.catalogContent(selectedId: String?): FilterCatalogContent = when (this) {
    IndustryResultUiState.Loading -> FilterCatalogContent.Loading
    is IndustryResultUiState.Content -> FilterCatalogContent.Items(
        values = items.map { industry ->
            FilterCatalogItem(
                id = industry.id,
                name = industry.name,
                isSelected = industry.id == selectedId,
            )
        },
    )
    IndustryResultUiState.Empty -> FilterCatalogContent.Empty
    IndustryResultUiState.NoInternet -> FilterCatalogContent.NoInternet
    IndustryResultUiState.GenericError -> FilterCatalogContent.GenericError
}

private fun AreaResultUiState.catalogContent(selectedId: Int?): FilterCatalogContent = when (this) {
    AreaResultUiState.Loading -> FilterCatalogContent.Loading
    is AreaResultUiState.Content -> FilterCatalogContent.Items(
        values = items.map { area ->
            FilterCatalogItem(
                id = area.id.toString(),
                name = area.name,
                isSelected = area.id == selectedId,
            )
        },
    )
    AreaResultUiState.Empty -> FilterCatalogContent.Empty
    AreaResultUiState.NoInternet -> FilterCatalogContent.NoInternet
    AreaResultUiState.GenericError -> FilterCatalogContent.GenericError
}

private fun workplaceName(country: Area?, region: Area?): String? = listOfNotNull(
    country?.name?.takeIf(String::isNotBlank),
    region?.name?.takeIf(String::isNotBlank),
).joinToString(separator = ", ").takeIf(String::isNotBlank)

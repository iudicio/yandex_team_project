package ru.practicum.android.diploma.ui.filter

internal data class FilterScreenModel(
    val salaryInput: String,
    val onlyWithSalary: Boolean,
    val industryName: String?,
    val workplaceName: String?,
    val hasActiveFilters: Boolean,
    val canApply: Boolean,
)

internal data class IndustryScreenModel(
    val query: String,
    val content: FilterCatalogContent,
    val hasSelection: Boolean,
)

internal data class WorkplaceScreenModel(
    val countryName: String?,
    val regionName: String?,
    val hasSelection: Boolean,
)

internal data class CountryScreenModel(
    val content: FilterCatalogContent,
)

internal data class RegionScreenModel(
    val query: String,
    val content: FilterCatalogContent,
)

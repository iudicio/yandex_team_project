package ru.practicum.android.diploma.presentation.filter

data class FilterUiState(
    val salary: String = "",
    val countryId: Int? = null,
    val countryName: String? = null,
    val regionId: Int? = null,
    val regionName: String? = null,
    val industryId: String? = null,
    val industryName: String? = null,
    val onlyWithSalary: Boolean = false,
) {
    val workplaceName: String?
        get() = listOfNotNull(countryName, regionName)
            .distinct()
            .takeIf(List<String>::isNotEmpty)
            ?.joinToString(", ")

    val hasActiveFilters: Boolean
        get() = salary.isNotEmpty() ||
            countryId != null ||
            regionId != null ||
            industryId != null ||
            onlyWithSalary
}

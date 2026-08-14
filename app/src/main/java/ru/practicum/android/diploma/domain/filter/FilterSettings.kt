package ru.practicum.android.diploma.domain.filter

data class FilterSettings(
    val salary: String = "",
    val industryId: String? = null,
    val industryName: String? = null,
    val countryId: Int? = null,
    val countryName: String? = null,
    val regionId: Int? = null,
    val regionName: String? = null,
    val onlyWithSalary: Boolean = false,
) {
    val areaId: Int?
        get() = regionId ?: countryId

    val hasActiveFilters: Boolean
        get() = salary.isNotEmpty() ||
            industryId != null ||
            areaId != null ||
            onlyWithSalary
}

package ru.practicum.android.diploma.domain.filter

data class Industry(
    val id: String,
    val name: String,
)

data class Area(
    val id: Int,
    val name: String,
    val parentId: Int? = null,
    val areas: List<Area> = emptyList(),
)

fun Area.toSelection(): Area = copy(parentId = null, areas = emptyList())

data class FilterSettings(
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false,
    val industry: Industry? = null,
    val country: Area? = null,
    val region: Area? = null,
) {
    val industryId: String?
        get() = industry?.id

    val areaId: Int?
        get() = region?.id ?: country?.id

    val hasActiveFilters: Boolean
        get() = salary != null || onlyWithSalary || industry != null || areaId != null
}

sealed interface FilterCatalogOutcome<out T> {
    data class Success<T>(val value: T) : FilterCatalogOutcome<T>
    data class Failure(val error: FilterCatalogError) : FilterCatalogOutcome<Nothing>
}

sealed interface FilterCatalogError {
    data object NoInternet : FilterCatalogError
    data object Generic : FilterCatalogError
}

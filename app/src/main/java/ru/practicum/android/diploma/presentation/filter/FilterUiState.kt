package ru.practicum.android.diploma.presentation.filter

import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.Industry

data class FilterUiState(
    val salaryInput: String = "",
    val onlyWithSalary: Boolean = false,
    val industry: Industry? = null,
    val country: Area? = null,
    val region: Area? = null,
    val hasActiveFilters: Boolean = false,
    val canApply: Boolean = false,
)

sealed interface FilterEvent {
    data class Applied(val settings: FilterSettings) : FilterEvent
    data object Close : FilterEvent
}

internal fun FilterSettings.toFilterUiState(
    salaryInput: String = salary?.toString().orEmpty(),
    canApply: Boolean = false,
): FilterUiState = FilterUiState(
    salaryInput = salaryInput,
    onlyWithSalary = onlyWithSalary,
    industry = industry,
    country = country,
    region = region,
    hasActiveFilters = hasActiveFilters,
    canApply = canApply,
)

package ru.practicum.android.diploma.domain.search

import ru.practicum.android.diploma.domain.filter.FilterSettings

data class VacancySearchRequest(
    val text: String,
    val area: Int? = null,
    val industry: Int? = null,
    val salary: Int? = null,
    val page: Int = FIRST_PAGE,
    val onlyWithSalary: Boolean? = null,
) {
    init {
        require(text.isNotBlank()) { "Search text must not be blank" }
        require(page >= FIRST_PAGE) { "Page must be positive" }
    }
}

fun FilterSettings.toVacancySearchRequest(
    text: String,
    page: Int = FIRST_PAGE,
): VacancySearchRequest = VacancySearchRequest(
    text = text.trim(),
    area = areaId,
    industry = industryId?.toIntOrNull(),
    salary = salary.toIntOrNull(),
    page = page,
    onlyWithSalary = if (onlyWithSalary) true else null,
)

private const val FIRST_PAGE = 1

package ru.practicum.android.diploma.data.search.dto

data class VacancySearchRequestDto(
    val text: String,
    val page: Int,
    val salary: Int?,
    val onlyWithSalary: Boolean?,
    val industryId: String?,
    val areaId: Int?,
)

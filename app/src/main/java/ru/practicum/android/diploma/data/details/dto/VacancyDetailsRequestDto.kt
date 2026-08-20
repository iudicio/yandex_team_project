package ru.practicum.android.diploma.data.details.dto

data class VacancyDetailsRequestDto(
    val vacancyId: String,
) {
    init {
        require(vacancyId.isNotBlank()) { "Vacancy id must not be blank" }
    }
}

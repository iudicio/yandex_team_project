package ru.practicum.android.diploma.domain.search

data class VacancyCard(
    val id: String,
    val name: String,
    val company: String? = null,
    val city: String? = null,
    val salary: Salary? = null,
    val logo: String? = null,
)

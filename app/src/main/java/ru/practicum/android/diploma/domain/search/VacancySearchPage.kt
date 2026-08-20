package ru.practicum.android.diploma.domain.search

data class VacancySearchPage(
    val found: Int,
    val pages: Int,
    val page: Int,
    val items: List<VacancyCard>,
)

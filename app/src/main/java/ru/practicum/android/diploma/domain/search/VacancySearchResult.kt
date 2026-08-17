package ru.practicum.android.diploma.domain.search

data class VacancySearchResult(
    val found: Int,
    val pages: Int,
    val page: Int,
    val items: List<VacancyCard>,
)

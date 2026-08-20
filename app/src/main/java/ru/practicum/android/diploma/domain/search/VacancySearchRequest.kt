package ru.practicum.android.diploma.domain.search

data class VacancySearchRequest(
    val text: String,
    val page: Int = FIRST_PAGE,
) {
    init {
        require(text.isNotBlank()) { "Search text must not be blank" }
        require(page >= FIRST_PAGE) { "Page must be at least $FIRST_PAGE" }
    }

    companion object {
        const val FIRST_PAGE = 1
    }
}

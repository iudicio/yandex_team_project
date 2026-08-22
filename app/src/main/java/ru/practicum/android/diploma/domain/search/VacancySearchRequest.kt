package ru.practicum.android.diploma.domain.search

data class VacancySearchRequest(
    val text: String,
    val page: Int = FIRST_PAGE,
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false,
    val industryId: String? = null,
    val areaId: Int? = null,
) {
    init {
        require(text.isNotBlank()) { "Search text must not be blank" }
        require(page >= FIRST_PAGE) { "Page must be at least $FIRST_PAGE" }
        require(salary == null || salary >= 0) { "Salary must not be negative" }
        require(industryId == null || industryId.isNotBlank()) { "Industry ID must not be blank" }
        require(areaId == null || areaId > 0) { "Area ID must be positive" }
    }

    companion object {
        const val FIRST_PAGE = 1
    }
}

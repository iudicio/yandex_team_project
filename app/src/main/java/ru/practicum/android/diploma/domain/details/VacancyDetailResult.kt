package ru.practicum.android.diploma.domain.details

import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.Salary

data class VacancyDetailResult(
    val id: String,
    val name: String,
    val description: String, // HTML
    val salary: Salary?,
    val address: Address?,
    val experience: BaseDetailData?,
    val schedule: BaseDetailData?,
    val employment: BaseDetailData?,
    val contacts: Contacts?,
    val employer: Employer,
    val area: Area?,
    val skills: List<String>,
    val url: String,
    val industry: Industry?
)

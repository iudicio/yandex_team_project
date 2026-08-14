package ru.practicum.android.diploma.presentation.details

import ru.practicum.android.diploma.ui.details.VacancyDetailsMock

data class DetailsUiState(
    val vacancy: VacancyDetailsMock = VacancyDetailsMock(
        id = "",
        title = "",
        salary = "",
        employerName = "",
        employerLogoUrl = null,
        city = "",
        experience = "",
        employment = "",
        schedule = "",
        description = "",
        keySkills = emptyList(),
        contactName = null,
        contactEmail = null,
        contactPhone = null,
        contactComment = null
    ),
    val isFavorite: Boolean = false
)

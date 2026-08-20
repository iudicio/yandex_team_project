package ru.practicum.android.diploma.domain.details

data class Contacts(
    val id: String?,
    val name: String?,
    val email: String?,
    val phones: List<Phone>,
)

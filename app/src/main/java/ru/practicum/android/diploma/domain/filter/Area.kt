package ru.practicum.android.diploma.domain.filter

data class Area(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val areas: List<Area>,
)

data class AreaSelection(
    val id: Int,
    val name: String,
)

data class RegionSelection(
    val id: Int,
    val name: String,
    val countryId: Int,
    val countryName: String,
)

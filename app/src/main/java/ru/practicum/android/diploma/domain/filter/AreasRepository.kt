package ru.practicum.android.diploma.domain.filter

interface AreasRepository {
    suspend fun loadAreas(): FilterCatalogOutcome<List<Area>>
}

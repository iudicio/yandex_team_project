package ru.practicum.android.diploma.domain.filter

interface IndustriesRepository {
    suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>>
}

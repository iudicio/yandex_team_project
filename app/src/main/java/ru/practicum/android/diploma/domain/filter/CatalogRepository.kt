package ru.practicum.android.diploma.domain.filter

interface CatalogRepository {
    suspend fun getCountries(): Result<List<AreaSelection>>
    suspend fun getRegions(countryId: Int?): Result<List<RegionSelection>>
    suspend fun getIndustries(): Result<List<Industry>>
}

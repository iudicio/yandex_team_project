package ru.practicum.android.diploma.data.filter

import retrofit2.http.GET
import ru.practicum.android.diploma.data.filter.dto.IndustryDto

interface IndustriesApi {
    @GET("industries")
    suspend fun getIndustries(): List<IndustryDto?>
}

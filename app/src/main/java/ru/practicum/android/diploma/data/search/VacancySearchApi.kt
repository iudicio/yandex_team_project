package ru.practicum.android.diploma.data.search

import retrofit2.http.GET
import retrofit2.http.Query
import ru.practicum.android.diploma.data.search.dto.VacancySearchResponseDto

interface VacancySearchApi {
    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") text: String,
        @Query("page") page: Int,
    ): VacancySearchResponseDto
}

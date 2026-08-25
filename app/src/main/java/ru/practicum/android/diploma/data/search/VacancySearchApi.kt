package ru.practicum.android.diploma.data.search

import retrofit2.http.GET
import retrofit2.http.Query
import ru.practicum.android.diploma.data.search.dto.VacancySearchResponseDto

interface VacancySearchApi {
    @Suppress("detekt.LongParameterList")
    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") text: String,
        @Query("page") page: Int,
        @Query("salary") salary: Int?,
        @Query("only_with_salary") onlyWithSalary: Boolean?,
        @Query("industry") industryId: String?,
        @Query("area") areaId: Int?,
    ): VacancySearchResponseDto
}

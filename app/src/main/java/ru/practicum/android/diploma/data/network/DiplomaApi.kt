package ru.practicum.android.diploma.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.data.network.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.network.dto.VacancyResponseDto

interface DiplomaApi {
    @GET("areas")
    suspend fun getAreas(): List<AreaDto>

    @GET("industries")
    suspend fun getIndustries(): List<IndustryDto>

    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") text: String,
        @Query("area") area: Int? = null,
        @Query("industry") industry: Int? = null,
        @Query("salary") salary: Int? = null,
        @Query("page") page: Int? = null,
        @Query("only_with_salary") onlyWithSalary: Boolean? = null,
    ): VacancyResponseDto

    @GET("vacancies/{vacancyId}")
    suspend fun getVacancy(@Path("vacancyId") id : String): VacancyDetailResponseDto

}

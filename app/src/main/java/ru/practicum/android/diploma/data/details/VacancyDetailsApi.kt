package ru.practicum.android.diploma.data.details

import retrofit2.http.GET
import retrofit2.http.Path
import ru.practicum.android.diploma.data.details.dto.VacancyDetailResponseDto

interface VacancyDetailsApi {
    @GET("vacancies/{vacancyId}")
    suspend fun getVacancy(
        @Path("vacancyId") vacancyId: String,
    ): VacancyDetailResponseDto
}

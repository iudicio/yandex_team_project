package ru.practicum.android.diploma.data.filter

import retrofit2.http.GET
import ru.practicum.android.diploma.data.filter.dto.AreaDto

interface AreasApi {
    @GET("areas")
    suspend fun getAreas(): List<AreaDto?>
}

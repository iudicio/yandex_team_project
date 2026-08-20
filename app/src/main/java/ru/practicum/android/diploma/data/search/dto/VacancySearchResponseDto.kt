package ru.practicum.android.diploma.data.search.dto

import com.google.gson.annotations.SerializedName

data class VacancySearchResponseDto(
    @SerializedName("found") val found: Int?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("items") val items: List<VacancyCardDto?>?,
)

data class VacancyCardDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("company") val company: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("salary") val salary: VacancySalaryDto?,
    @SerializedName("logo") val logo: String?,
)

data class VacancySalaryDto(
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?,
    @SerializedName("currency") val currency: String?,
)

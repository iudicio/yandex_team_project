package ru.practicum.android.diploma.data.network.dto

import com.google.gson.annotations.SerializedName

data class VacancyResponseDto(
    @SerializedName("found")
    val found: Int?,
    @SerializedName("pages")
    val pages: Int?,
    @SerializedName("page")
    val page: Int?,
    @SerializedName("items")
    val items: List<VacancyCardDto?>?,
)

data class VacancyCardDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("company")
    val company: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("salary")
    val salary: VacancyCardSalaryDto?,
    @SerializedName("logo")
    val logo: String?,
)

data class VacancyCardSalaryDto(
    @SerializedName("from")
    val from: Int?,
    @SerializedName("to")
    val to: Int?,
    @SerializedName("currency")
    val currency: String?,
)

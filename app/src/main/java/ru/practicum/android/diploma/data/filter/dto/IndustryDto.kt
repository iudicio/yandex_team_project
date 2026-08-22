package ru.practicum.android.diploma.data.filter.dto

import com.google.gson.annotations.SerializedName

data class IndustryDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("industries") val industries: List<IndustryDto?>? = null,
)

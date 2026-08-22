package ru.practicum.android.diploma.data.filter.dto

import com.google.gson.annotations.SerializedName

data class AreaDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName(value = "parentId", alternate = ["parent_id"])
    val parentId: Int? = null,
    @SerializedName("areas") val areas: List<AreaDto?>? = null,
)

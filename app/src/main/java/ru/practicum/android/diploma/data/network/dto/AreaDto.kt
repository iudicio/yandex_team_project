package ru.practicum.android.diploma.data.network.dto

import com.google.gson.annotations.SerializedName

data class AreaDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName(value = "parentId", alternate = ["parent_id"])
    val parentId: Int?,
    @SerializedName("areas")
    val areas: List<AreaDto>?,
)

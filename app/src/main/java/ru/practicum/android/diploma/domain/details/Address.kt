package ru.practicum.android.diploma.domain.details

import com.google.gson.annotations.SerializedName

data class Address(
    val id: String,
    val city: String,
    val street: String,
    val building: String,
    val raw: String
)

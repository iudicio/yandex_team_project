package ru.practicum.android.diploma.data.network.dto

import com.google.gson.annotations.SerializedName

data class VacancyDetailResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String, // HTML
    @SerializedName("salary")
    val salary: SalaryDto?,
    @SerializedName("address")
    val address: AddressDto?,
    @SerializedName("experience")
    val experience: BaseDetailDataDto?,
    @SerializedName("schedule")
    val schedule: BaseDetailDataDto?,
    @SerializedName("employment")
    val employment: BaseDetailDataDto?,
    @SerializedName("contacts")
    val contacts: ContactsDto?,
    @SerializedName("employer")
    val employer: EmployerDto,
    @SerializedName("area")
    val area: AreaDto,
    @SerializedName("skills")
    val skills: List<String>,
    @SerializedName("url")
    val url: String,
    @SerializedName("industry")
    val industry: IndustryDto
)

data class SalaryDto(
    @SerializedName("from")
    val from: Int?,
    @SerializedName("to")
    val to: Int?,
    @SerializedName("currency")
    val currency: String?
)

data class EmployerDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("logo")
    val logo: String
)

data class BaseDetailDataDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
)

data class ContactsDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phones")
    val phones: List<PhoneDto>
)

data class PhoneDto(
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("formatted")
    val formatted: String
)

data class AddressDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("street")
    val street: String,
    @SerializedName("building")
    val building: String,
    @SerializedName("raw")
    val raw: String
)


package ru.practicum.android.diploma.data.details.dto

import com.google.gson.annotations.SerializedName

data class VacancyDetailResponseDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("salary")
    val salary: SalaryDto? = null,
    @SerializedName("address")
    val address: AddressDto? = null,
    @SerializedName("experience")
    val experience: BaseDetailDataDto? = null,
    @SerializedName("schedule")
    val schedule: BaseDetailDataDto? = null,
    @SerializedName("employment")
    val employment: BaseDetailDataDto? = null,
    @SerializedName("contacts")
    val contacts: ContactsDto? = null,
    @SerializedName("employer")
    val employer: EmployerDto? = null,
    @SerializedName("area")
    val area: AreaDto? = null,
    @SerializedName("skills")
    val skills: List<String?>? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("industry")
    val industry: IndustryDto? = null,
)

data class SalaryDto(
    @SerializedName("from")
    val from: Int? = null,
    @SerializedName("to")
    val to: Int? = null,
    @SerializedName("currency")
    val currency: String? = null,
)

data class AddressDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("street")
    val street: String? = null,
    @SerializedName("building")
    val building: String? = null,
    @SerializedName("raw")
    val raw: String? = null,
)

data class BaseDetailDataDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
)

data class ContactsDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("phones")
    val phones: List<PhoneDto?>? = null,
)

data class PhoneDto(
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("formatted")
    val formatted: String? = null,
)

data class EmployerDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("logo")
    val logo: String? = null,
)

data class AreaDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
)

data class IndustryDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
)

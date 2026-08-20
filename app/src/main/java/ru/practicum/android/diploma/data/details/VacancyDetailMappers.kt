package ru.practicum.android.diploma.data.details

import ru.practicum.android.diploma.data.details.dto.AddressDto
import ru.practicum.android.diploma.data.details.dto.AreaDto
import ru.practicum.android.diploma.data.details.dto.BaseDetailDataDto
import ru.practicum.android.diploma.data.details.dto.ContactsDto
import ru.practicum.android.diploma.data.details.dto.EmployerDto
import ru.practicum.android.diploma.data.details.dto.IndustryDto
import ru.practicum.android.diploma.data.details.dto.PhoneDto
import ru.practicum.android.diploma.data.details.dto.SalaryDto
import ru.practicum.android.diploma.data.details.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Industry
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.Salary

internal fun VacancyDetailResponseDto.toDomain(fallbackId: String): VacancyDetailResult = VacancyDetailResult(
    id = id.normalized() ?: fallbackId,
    name = name.normalized().orEmpty(),
    description = description.orEmpty(),
    salary = salary?.toDomain(),
    address = address?.toDomain(),
    experience = experience?.toDomain(),
    schedule = schedule?.toDomain(),
    employment = employment?.toDomain(),
    contacts = contacts?.toDomain(),
    employer = employer?.toDomain() ?: Employer(id = null, name = null, logo = null),
    area = area?.toDomain(),
    skills = skills.orEmpty().mapNotNull { skill -> skill.normalized() },
    url = url.normalized().orEmpty(),
    industry = industry?.toDomain(),
)

private fun SalaryDto.toDomain(): Salary? {
    val normalizedCurrency = currency.normalized()
    return Salary(from = from, to = to, currency = normalizedCurrency)
        .takeIf { from != null || to != null || normalizedCurrency != null }
}

private fun AddressDto.toDomain(): Address? {
    val result = Address(
        id = id.normalized(),
        city = city.normalized(),
        street = street.normalized(),
        building = building.normalized(),
        raw = raw.normalized(),
    )
    return result.takeIf { address ->
        address.id != null || address.city != null || address.street != null ||
            address.building != null || address.raw != null
    }
}

private fun BaseDetailDataDto.toDomain(): BaseDetailData? {
    val normalizedId = id.normalized()
    val normalizedName = name.normalized()
    return BaseDetailData(id = normalizedId, name = normalizedName)
        .takeIf { normalizedId != null || normalizedName != null }
}

private fun ContactsDto.toDomain(): Contacts? {
    val normalizedName = name.normalized()
    val normalizedEmail = email.normalized()
    val normalizedPhones = phones.orEmpty().mapNotNull { phone -> phone?.toDomain() }
    return Contacts(
        id = id.normalized(),
        name = normalizedName,
        email = normalizedEmail,
        phones = normalizedPhones,
    ).takeIf { normalizedName != null || normalizedEmail != null || normalizedPhones.isNotEmpty() }
}

private fun PhoneDto.toDomain(): Phone? {
    val normalizedComment = comment.normalized()
    val normalizedPhone = formatted.normalized()
    return Phone(comment = normalizedComment, formatted = normalizedPhone)
        .takeIf { normalizedComment != null || normalizedPhone != null }
}

private fun EmployerDto.toDomain(): Employer = Employer(
    id = id.normalized(),
    name = name.normalized(),
    logo = logo.normalized(),
)

private fun AreaDto.toDomain(): Area? {
    val normalizedName = name.normalized()
    return Area(id = id, name = normalizedName).takeIf { id != null || normalizedName != null }
}

private fun IndustryDto.toDomain(): Industry? {
    val normalizedName = name.normalized()
    return Industry(id = id, name = normalizedName).takeIf { id != null || normalizedName != null }
}

private fun String?.normalized(): String? = this?.trim()?.takeIf(String::isNotEmpty)

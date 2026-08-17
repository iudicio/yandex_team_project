package ru.practicum.android.diploma.data.details

import com.google.gson.Gson
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.data.network.dto.AddressDto
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.BaseDetailDataDto
import ru.practicum.android.diploma.data.network.dto.ContactsDto
import ru.practicum.android.diploma.data.network.dto.EmployerDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.data.network.dto.PhoneDto
import ru.practicum.android.diploma.data.network.dto.SalaryDto
import ru.practicum.android.diploma.data.network.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.Salary
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty

internal fun VacancyDetailResponseDto.toDomain(): VacancyDetailResult {
    return VacancyDetailResult(
        id = id,
        name = name,
        description = description,
        salary = salary?.toDomain(),
        address = address?.toDomain(),
        experience = experience?.toDomain(),
        schedule = schedule?.toDomain(),
        employment = employment?.toDomain(),
        contacts = contacts?.toDomain(),
        employer = employer.toDomain(),
        area = area.toDomain(null),
        skills = skills,
        url = url,
        industry = industry.toDomain(),
    )
}

internal fun SalaryDto.toDomain(): Salary = Salary(
    from = from,
    to = to,
    currency = currency?.takeIf(String::isNotBlank),
)

internal fun AddressDto.toDomain(): Address = Address(
    id = id,
    city = city,
    street = street,
    building = building,
    raw = raw
)

internal fun BaseDetailDataDto.toDomain(): BaseDetailData = BaseDetailData(
    id = id,
    name = name
)

internal fun ContactsDto.toDomain(): Contacts = Contacts(
    id = id,
    name = name,
    email = email,
    phones = phones.map { it.toDomain() }
)

internal fun PhoneDto.toDomain(): Phone = Phone(
    comment = comment,
    formatted = formatted
)

internal fun EmployerDto.toDomain(): Employer = Employer(
    id = id,
    name = name,
    logo = logo
)

internal fun AreaDto.toDomain(inheritedParentId: Int?): Area? {
    val validId = id
    val validName = name?.takeIf(String::isNotBlank)
    return if (validId == null || validName == null) {
        null
    } else {
        Area(
            id = validId,
            name = validName,
            parentId = parentId ?: inheritedParentId,
            areas = areas.orEmpty().mapNotNull { child ->
                child.toDomain(inheritedParentId = validId)
            },
        )
    }
}

internal fun IndustryDto.toDomain(): Industry? {
    val validId = id
    val validName = name?.takeIf(String::isNotBlank)
    return if (validId == null || validName == null) {
        null
    } else {
        Industry(
            id = validId.toString(),
            name = validName,
        )
    }
}

internal fun FavoriteVacancyEntity.toDetail(gson: Gson): VacancyDetailResult = VacancyDetailResult(
    id = id,
    name = name,
    description = description,
    salary = salaryJson?.let { gson.fromJson(it, SalaryDto::class.java).toDomain() },
    address = addressJson?.let { gson.fromJson(it, AddressDto::class.java).toDomain() },
    experience = baseDetailDataOrNull(experienceId, experienceName),
    schedule = baseDetailDataOrNull(scheduleId, scheduleName),
    employment = baseDetailDataOrNull(employmentId, employmentName),
    contacts = contactsJson?.let { gson.fromJson(it, ContactsDto::class.java).toDomain() },
    employer = gson.fromJson(employerJson, EmployerDto::class.java).toDomain(),
    area = areaJson?.let { gson.fromJson(it, AreaDto::class.java).toDomain(null) },
    skills = gson.fromJson(skillsJson, Array<String>::class.java)?.toList().orEmpty(),
    url = url,
    industry = industryOrNull(industryId, industryName),
)

private fun baseDetailDataOrNull(id: String?, name: String?): BaseDetailData? =
    if (id != null && name != null) BaseDetailData(id = id, name = name) else null

private fun industryOrNull(id: String?, name: String?): Industry? =
    if (id != null && name != null) Industry(id = id, name = name) else null

internal fun FavoriteVacancyEntity.toCard(gson: Gson): FavoriteVacancy {
    val employer = gson.fromJson(employerJson, EmployerDto::class.java)
    return FavoriteVacancy(
        id = id,
        name = name,
        company = employer?.name?.takeIf(String::isNotBlank),
        city = areaJson?.let { gson.fromJson(it, AreaDto::class.java) }?.name?.takeIf(String::isNotBlank),
        salary = salaryJson?.let { gson.fromJson(it, SalaryDto::class.java).toDomain() },
        logo = employer?.logo?.takeIf(String::isNotBlank),
    )
}

internal fun VacancyDetailResult.toEntity(gson: Gson): FavoriteVacancyEntity = FavoriteVacancyEntity(
    id = id,
    name = name,
    description = description,
    salaryJson = salary?.let { gson.toJson(SalaryDto(from = it.from, to = it.to, currency = it.currency)) },
    addressJson = address?.let {
        gson.toJson(AddressDto(id = it.id, city = it.city, street = it.street, building = it.building, raw = it.raw))
    },
    experienceId = experience?.id,
    experienceName = experience?.name,
    scheduleId = schedule?.id,
    scheduleName = schedule?.name,
    employmentId = employment?.id,
    employmentName = employment?.name,
    contactsJson = contacts?.let {
        gson.toJson(
            ContactsDto(
                id = it.id,
                name = it.name,
                email = it.email,
                phones = it.phones.map { phone -> PhoneDto(comment = phone.comment, formatted = phone.formatted) },
            ),
        )
    },
    employerJson = gson.toJson(EmployerDto(id = employer.id, name = employer.name, logo = employer.logo)),
    areaJson = area?.let { gson.toJson(AreaDto(id = it.id, name = it.name, parentId = it.parentId, areas = null)) },
    skillsJson = gson.toJson(skills),
    url = url,
    industryId = industry?.id,
    industryName = industry?.name,
)

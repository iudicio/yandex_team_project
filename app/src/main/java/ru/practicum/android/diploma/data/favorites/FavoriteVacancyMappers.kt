package ru.practicum.android.diploma.data.favorites

import ru.practicum.android.diploma.data.db.entity.FavoritePhoneSnapshot
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Industry
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.VacancyCard

internal fun VacancyDetailResult.toFavoriteEntity(
    addedAt: Long = System.currentTimeMillis(),
): FavoriteVacancyEntity = FavoriteVacancyEntity(
    id = id,
    name = name,
    description = description,
    salaryFrom = salary?.from,
    salaryTo = salary?.to,
    salaryCurrency = salary?.currency,
    addressId = address?.id,
    addressCity = address?.city,
    addressStreet = address?.street,
    addressBuilding = address?.building,
    addressRaw = address?.raw,
    experienceId = experience?.id,
    experienceName = experience?.name,
    scheduleId = schedule?.id,
    scheduleName = schedule?.name,
    employmentId = employment?.id,
    employmentName = employment?.name,
    contactsId = contacts?.id,
    contactsName = contacts?.name,
    contactsEmail = contacts?.email,
    contactsPhones = contacts?.phones.orEmpty().map { phone -> phone.toSnapshot() },
    employerId = employer.id,
    employerName = employer.name,
    employerLogo = employer.logo,
    areaId = area?.id,
    areaName = area?.name,
    skills = skills,
    url = url,
    industryId = industry?.id,
    industryName = industry?.name,
    addedAt = addedAt,
)

internal fun FavoriteVacancyEntity.toVacancyCard(): VacancyCard = VacancyCard(
    id = id,
    name = name,
    company = employerName,
    city = addressCity ?: areaName,
    salary = salaryOrNull(),
    logo = employerLogo,
)

internal fun FavoriteVacancyEntity.toVacancyDetails(): VacancyDetailResult = VacancyDetailResult(
    id = id,
    name = name,
    description = description,
    salary = salaryOrNull(),
    address = addressOrNull(),
    experience = baseDetailOrNull(experienceId, experienceName),
    schedule = baseDetailOrNull(scheduleId, scheduleName),
    employment = baseDetailOrNull(employmentId, employmentName),
    contacts = contactsOrNull(),
    employer = Employer(
        id = employerId,
        name = employerName,
        logo = employerLogo,
    ),
    area = areaOrNull(),
    skills = skills,
    url = url,
    industry = industryOrNull(),
)

private fun Phone.toSnapshot(): FavoritePhoneSnapshot = FavoritePhoneSnapshot(
    comment = comment,
    formatted = formatted,
)

private fun FavoriteVacancyEntity.salaryOrNull(): Salary? = if (
    salaryFrom != null || salaryTo != null || !salaryCurrency.isNullOrBlank()
) {
    Salary(from = salaryFrom, to = salaryTo, currency = salaryCurrency)
} else {
    null
}

private fun FavoriteVacancyEntity.addressOrNull(): Address? = if (
    listOf(addressId, addressCity, addressStreet, addressBuilding, addressRaw).any { value -> value != null }
) {
    Address(
        id = addressId,
        city = addressCity,
        street = addressStreet,
        building = addressBuilding,
        raw = addressRaw,
    )
} else {
    null
}

private fun FavoriteVacancyEntity.contactsOrNull(): Contacts? {
    val hasContactMetadata = listOf(contactsId, contactsName, contactsEmail).any { value -> value != null }
    return if (hasContactMetadata || contactsPhones.isNotEmpty()) {
        Contacts(
            id = contactsId,
            name = contactsName,
            email = contactsEmail,
            phones = contactsPhones.map { phone ->
                Phone(comment = phone.comment, formatted = phone.formatted)
            },
        )
    } else {
        null
    }
}

private fun FavoriteVacancyEntity.areaOrNull(): Area? = if (areaId != null || areaName != null) {
    Area(id = areaId, name = areaName)
} else {
    null
}

private fun FavoriteVacancyEntity.industryOrNull(): Industry? = if (industryId != null || industryName != null) {
    Industry(id = industryId, name = industryName)
} else {
    null
}

private fun baseDetailOrNull(id: String?, name: String?): BaseDetailData? = if (id != null || name != null) {
    BaseDetailData(id = id, name = name)
} else {
    null
}

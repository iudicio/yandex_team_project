package ru.practicum.android.diploma.data.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.practicum.android.diploma.data.details.dto.AddressDto
import ru.practicum.android.diploma.data.details.dto.AreaDto
import ru.practicum.android.diploma.data.details.dto.BaseDetailDataDto
import ru.practicum.android.diploma.data.details.dto.ContactsDto
import ru.practicum.android.diploma.data.details.dto.EmployerDto
import ru.practicum.android.diploma.data.details.dto.IndustryDto
import ru.practicum.android.diploma.data.details.dto.PhoneDto
import ru.practicum.android.diploma.data.details.dto.SalaryDto
import ru.practicum.android.diploma.data.details.dto.VacancyDetailResponseDto

class VacancyDetailMappersTest {

    @Test
    fun `missing response fields use safe values and requested id`() {
        val result = VacancyDetailResponseDto().toDomain(fallbackId = "requested-id")

        assertEquals("requested-id", result.id)
        assertEquals("", result.name)
        assertEquals("", result.description)
        assertEquals("", result.url)
        assertNull(result.salary)
        assertNull(result.address)
        assertNull(result.contacts)
        assertNull(result.area)
        assertNull(result.industry)
        assertEquals(emptyList<String>(), result.skills)
        assertNull(result.employer.name)
        assertNull(result.employer.logo)
    }

    @Test
    fun `nested fields are trimmed and empty optional sections are removed`() {
        val response = VacancyDetailResponseDto(
            id = " 42 ",
            name = " Android developer ",
            description = "<p>Kotlin</p>",
            salary = SalaryDto(from = 100_000, currency = " RUR "),
            address = AddressDto(city = " Москва ", raw = " "),
            experience = BaseDetailDataDto(id = "exp", name = " "),
            contacts = ContactsDto(
                id = "ignored-id",
                phones = listOf(
                    null,
                    PhoneDto(comment = " 10:00–18:00 ", formatted = " +7 999 000-00-00 "),
                    PhoneDto(),
                ),
            ),
            employer = EmployerDto(id = " employer ", name = " Company ", logo = " https://logo.test/a.png "),
            area = AreaDto(id = 1, name = " Москва "),
            skills = listOf(null, " ", " Kotlin "),
            url = " https://example.test/vacancy/42 ",
            industry = IndustryDto(id = 7, name = " IT "),
        )

        val result = response.toDomain(fallbackId = "fallback")

        assertEquals("42", result.id)
        assertEquals("Android developer", result.name)
        assertEquals(100_000, result.salary?.from)
        assertEquals("RUR", result.salary?.currency)
        assertEquals("Москва", result.address?.city)
        assertNull(result.experience?.name)
        assertEquals("10:00–18:00", result.contacts?.phones?.single()?.comment)
        assertEquals("+7 999 000-00-00", result.contacts?.phones?.single()?.formatted)
        assertEquals("Company", result.employer.name)
        assertEquals("Москва", result.area?.name)
        assertEquals(listOf("Kotlin"), result.skills)
        assertEquals("https://example.test/vacancy/42", result.url)
        assertEquals(7, result.industry?.id)
    }

    @Test
    fun `objects without visible values map to null`() {
        val result = VacancyDetailResponseDto(
            salary = SalaryDto(),
            address = AddressDto(),
            experience = BaseDetailDataDto(),
            contacts = ContactsDto(id = "technical-id"),
            area = AreaDto(),
            industry = IndustryDto(),
        ).toDomain(fallbackId = "id")

        assertNull(result.salary)
        assertNull(result.address)
        assertNull(result.experience)
        assertNull(result.contacts)
        assertNull(result.area)
        assertNull(result.industry)
    }
}

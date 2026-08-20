package ru.practicum.android.diploma.ui.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult

class DetailsScreenFormattingTest {

    @Test
    fun `raw address has priority over city and region`() {
        val vacancy = fixture(
            address = address(raw = "Full address", city = "City"),
            area = Area(id = 1, name = "Region"),
        )

        assertEquals("Full address", vacancy.detailsLocation())
    }

    @Test
    fun `city then region are used as address fallbacks`() {
        val cityVacancy = fixture(
            address = address(raw = " ", city = "City"),
            area = Area(id = 1, name = "Region"),
        )
        val regionVacancy = fixture(
            address = address(raw = null, city = ""),
            area = Area(id = 1, name = "Region"),
        )

        assertEquals("City", cityVacancy.detailsLocation())
        assertEquals("Region", regionVacancy.detailsLocation())
        assertNull(fixture(address = null, area = null).detailsLocation())
    }

    @Test
    fun `contacts section is hidden when every field is blank`() {
        val hidden = Contacts(
            id = "server-id-only",
            name = " ",
            email = null,
            phones = listOf(Phone(comment = "", formatted = " ")),
        )
        val visibleByComment = hidden.copy(phones = listOf(Phone(comment = "after 10", formatted = null)))

        assertFalse(hidden.hasVisibleContent())
        assertTrue(visibleByComment.hasVisibleContent())
    }
}

private fun address(raw: String?, city: String?): Address = Address(
    id = null,
    city = city,
    street = null,
    building = null,
    raw = raw,
)

private fun fixture(
    address: Address?,
    area: Area?,
): VacancyDetailResult = VacancyDetailResult(
    id = "42",
    name = "Vacancy",
    description = "",
    salary = null,
    address = address,
    experience = null,
    schedule = null,
    employment = null,
    contacts = null,
    employer = Employer(id = null, name = null, logo = null),
    area = area,
    skills = emptyList(),
    url = "",
    industry = null,
)

package ru.practicum.android.diploma.data.favorites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Industry
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.Salary

class FavoriteVacancyMappersTest {
    @Test
    fun `full snapshot survives entity round trip`() {
        val details = fullDetails()

        val entity = details.toFavoriteEntity(addedAt = ADDED_AT)

        assertEquals(ADDED_AT, entity.addedAt)
        assertEquals(details, entity.toVacancyDetails())
        assertEquals(details.id, entity.toVacancyCard().id)
        assertEquals(details.employer.name, entity.toVacancyCard().company)
        assertEquals(details.address?.city, entity.toVacancyCard().city)
        assertEquals(details.salary, entity.toVacancyCard().salary)
        assertEquals(details.employer.logo, entity.toVacancyCard().logo)
    }

    @Test
    fun `empty optional snapshot restores null blocks`() {
        val details = fullDetails().copy(
            salary = null,
            address = null,
            experience = null,
            schedule = null,
            employment = null,
            contacts = null,
            area = null,
            skills = emptyList(),
            industry = null,
        )

        val restored = details.toFavoriteEntity(ADDED_AT).toVacancyDetails()

        assertEquals(details, restored)
        assertNull(restored.salary)
        assertNull(restored.address)
        assertNull(restored.contacts)
    }

    private companion object {
        const val ADDED_AT = 42L
    }
}

private fun fullDetails(): VacancyDetailResult = VacancyDetailResult(
    id = "vacancy-1",
    name = "Android-разработчик",
    description = "<p>Описание</p>",
    salary = Salary(from = 100_000, to = 150_000, currency = "RUR"),
    address = Address(
        id = "address-1",
        city = "Москва",
        street = "Льва Толстого",
        building = "16",
        raw = "Москва, улица Льва Толстого, 16",
    ),
    experience = BaseDetailData(id = "between1And3", name = "От 1 года до 3 лет"),
    schedule = BaseDetailData(id = "remote", name = "Удалённая работа"),
    employment = BaseDetailData(id = "full", name = "Полная занятость"),
    contacts = Contacts(
        id = "contacts-1",
        name = "Иван Иванов",
        email = "ivan@example.com",
        phones = listOf(
            Phone(comment = "По будням", formatted = "+7 999 000-00-00"),
            Phone(comment = null, formatted = "+7 999 111-11-11"),
        ),
    ),
    employer = Employer(id = "employer-1", name = "Компания", logo = "https://example.com/logo.png"),
    area = Area(id = 1, name = "Москва"),
    skills = listOf("Kotlin", "Coroutines"),
    url = "https://example.com/vacancy-1",
    industry = Industry(id = 7, name = "Информационные технологии"),
)

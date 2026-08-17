package ru.practicum.android.diploma.data.favorite

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.data.details.toCard
import ru.practicum.android.diploma.data.details.toDetail
import ru.practicum.android.diploma.domain.search.Salary

class FavoriteVacancyMappersTest {

    private val gson = Gson()

    @Test
    fun `entity with full salary json maps to card with salary`() {
        val entity = entity(
            salaryJson = """{"from":100000,"to":200000,"currency":"RUR"}""",
            employerJson = """{"id":"e1","name":"Yandex","logo":"https://logo.example/img"}""",
            areaJson = """{"id":1,"name":"Moscow"}""",
        )

        val card = entity.toCard(gson)

        assertEquals("Yandex", card.company)
        assertEquals("Moscow", card.city)
        assertEquals(Salary(from = 100_000, to = 200_000, currency = "RUR"), card.salary)
        assertEquals("https://logo.example/img", card.logo)
    }

    @Test
    fun `entity with only lower bound salary still produces salary`() {
        val entity = entity(
            salaryJson = """{"from":50000,"currency":"USD"}""",
        )

        val card = entity.toCard(gson)

        assertEquals(Salary(from = 50_000, currency = "USD"), card.salary)
    }

    @Test
    fun `entity without salary json maps to null salary`() {
        val entity = entity(salaryJson = null)

        val card = entity.toCard(gson)

        assertNull(card.salary)
    }

    @Test
    fun `entity maps to detail with all fields`() {
        val entity = entity(
            salaryJson = """{"from":1,"to":2,"currency":"USD"}""",
            employerJson = """{"id":"e1","name":"Yandex","logo":"https://logo.example/img"}""",
            areaJson = """{"id":1,"name":"Moscow"}""",
            skillsJson = """["Kotlin","Android"]""",
            experienceId = "exp1",
            experienceName = "1-3 years",
        )

        val detail = entity.toDetail(gson)

        assertEquals("42", detail.id)
        assertEquals("Android developer", detail.name)
        assertEquals(Salary(from = 1, to = 2, currency = "USD"), detail.salary)
        assertEquals("Yandex", detail.employer.name)
        assertEquals("Moscow", detail.area?.name)
        assertEquals(listOf("Kotlin", "Android"), detail.skills)
        assertEquals("1-3 years", detail.experience?.name)
    }

    private fun entity(
        salaryJson: String? = null,
        employerJson: String = """{"id":"e1","name":"Company"}""",
        areaJson: String? = null,
        skillsJson: String = "[]",
        experienceId: String? = null,
        experienceName: String? = null,
    ) = FavoriteVacancyEntity(
        id = "42",
        name = "Android developer",
        description = "Great job",
        salaryJson = salaryJson,
        addressJson = null,
        experienceId = experienceId,
        experienceName = experienceName,
        scheduleId = null,
        scheduleName = null,
        employmentId = null,
        employmentName = null,
        contactsJson = null,
        employerJson = employerJson,
        areaJson = areaJson,
        skillsJson = skillsJson,
        url = "https://vacancy.example/42",
        industryId = null,
        industryName = null,
    )
}

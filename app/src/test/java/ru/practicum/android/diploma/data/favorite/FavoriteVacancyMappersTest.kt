package ru.practicum.android.diploma.data.favorite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.practicum.android.diploma.data.favorites.FavoriteVacancyEntity
import ru.practicum.android.diploma.data.favorites.toDomain
import ru.practicum.android.diploma.data.favorites.toEntity
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.search.Salary

class FavoriteVacancyMappersTest {

    @Test
    fun `entity with full salary maps to domain salary`() {
        val entity = entity(salaryFrom = 100_000, salaryTo = 200_000, salaryCurrency = "RUR")

        assertEquals(Salary(from = 100_000, to = 200_000, currency = "RUR"), entity.toDomain().salary)
    }

    @Test
    fun `entity with only lower bound still produces salary`() {
        val entity = entity(salaryFrom = 50_000)

        assertEquals(Salary(from = 50_000), entity.toDomain().salary)
    }

    @Test
    fun `entity with all salary fields null maps to null salary`() {
        val entity = entity()

        assertNull(entity.toDomain().salary)
    }

    @Test
    fun `round trip preserves display fields`() {
        val domain = FavoriteVacancy(
            id = "42",
            name = "Android developer",
            company = "Yandex",
            city = "Moscow",
            salary = Salary(from = 1, to = 2, currency = "USD"),
            logo = "https://logo.example/png",
        )

        val restored = domain.toEntity(addedAt = 7).toDomain()

        assertEquals(domain, restored)
    }

    private fun entity(
        salaryFrom: Int? = null,
        salaryTo: Int? = null,
        salaryCurrency: String? = null,
    ) = FavoriteVacancyEntity(
        id = "1",
        name = "Developer",
        company = "Company",
        city = "City",
        salaryFrom = salaryFrom,
        salaryTo = salaryTo,
        salaryCurrency = salaryCurrency,
        logo = null,
        addedAt = 1,
    )
}

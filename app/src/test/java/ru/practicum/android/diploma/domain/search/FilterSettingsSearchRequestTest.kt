package ru.practicum.android.diploma.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.FilterSettings

class FilterSettingsSearchRequestTest {

    @Test
    fun `all valid filters are converted to search request`() {
        val settings = FilterSettings(
            salary = "00100000",
            industryId = "7",
            countryId = 1,
            regionId = 10,
            onlyWithSalary = true,
        )

        val request = settings.toVacancySearchRequest(text = "  Android developer  ", page = 3)

        assertEquals(
            VacancySearchRequest(
                text = "Android developer",
                area = 10,
                industry = 7,
                salary = 100000,
                page = 3,
                onlyWithSalary = true,
            ),
            request,
        )
    }

    @Test
    fun `country is used when region is not selected and false flag is omitted`() {
        val request = FilterSettings(
            countryId = 1,
            onlyWithSalary = false,
        ).toVacancySearchRequest(text = "Android")

        assertEquals(1, request.area)
        assertNull(request.industry)
        assertNull(request.salary)
        assertEquals(1, request.page)
        assertNull(request.onlyWithSalary)
    }

    @Test
    fun `invalid numeric filters are omitted instead of breaking request`() {
        val request = FilterSettings(
            salary = "12a3",
            industryId = "2147483648",
        ).toVacancySearchRequest(text = "Android")

        assertNull(request.salary)
        assertNull(request.industry)
    }

    @Test
    fun `blank query is rejected after trimming`() {
        assertThrows(IllegalArgumentException::class.java) {
            FilterSettings().toVacancySearchRequest(text = "   ")
        }
    }

    @Test
    fun `non-positive page is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FilterSettings().toVacancySearchRequest(text = "Android", page = 0)
        }
    }
}

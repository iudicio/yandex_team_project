package ru.practicum.android.diploma.data.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.data.network.dto.VacancyCardDto
import ru.practicum.android.diploma.data.network.dto.VacancyCardSalaryDto
import ru.practicum.android.diploma.data.network.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.network.dto.VacancyResponseDto
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.VacancySearchRequest

class RetrofitVacancyRepositoryTest {

    @Test
    fun `search passes every filter to api and maps valid vacancy cards`() = runBlocking {
        val api = FakeDiplomaApi(
            response = VacancyResponseDto(
                found = 2,
                pages = 4,
                page = 3,
                items = listOf(
                    VacancyCardDto(
                        id = "vacancy-1",
                        name = "Android developer",
                        company = "  ",
                        city = "Москва",
                        salary = VacancyCardSalaryDto(from = 100000, to = 150000, currency = "RUR"),
                        logo = "https://example.test/logo.png",
                    ),
                    VacancyCardDto(
                        id = " ",
                        name = "Malformed vacancy",
                        company = null,
                        city = null,
                        salary = null,
                        logo = null,
                    ),
                    null,
                ),
            ),
        )
        val repository = RetrofitVacancyRepository(api, Dispatchers.Unconfined)
        val request = VacancySearchRequest(
            text = "Android",
            area = 10,
            industry = 7,
            salary = 100000,
            page = 3,
            onlyWithSalary = true,
        )

        val result = repository.search(request).getOrThrow()

        assertEquals(request.toCapturedSearchCall(), api.lastCall)
        assertEquals(2, result.found)
        assertEquals(4, result.pages)
        assertEquals(3, result.page)
        assertEquals(
            listOf(
                VacancyCard(
                    id = "vacancy-1",
                    name = "Android developer",
                    company = null,
                    city = "Москва",
                    salary = Salary(from = 100000, to = 150000, currency = "RUR"),
                    logo = "https://example.test/logo.png",
                ),
            ),
            result.items,
        )
    }

    @Test
    fun `missing response fields use safe defaults and requested page`() = runBlocking {
        val api = FakeDiplomaApi(
            response = VacancyResponseDto(
                found = null,
                pages = null,
                page = null,
                items = null,
            ),
        )
        val repository = RetrofitVacancyRepository(api, Dispatchers.Unconfined)

        val result = repository.search(VacancySearchRequest(text = "Android", page = 5)).getOrThrow()

        assertEquals(0, result.found)
        assertEquals(0, result.pages)
        assertEquals(5, result.page)
        assertTrue(result.items.isEmpty())
        assertNull(api.lastCall?.area)
        assertNull(api.lastCall?.industry)
        assertNull(api.lastCall?.salary)
        assertNull(api.lastCall?.onlyWithSalary)
    }

    @Test
    fun `negative response counters are normalized to zero`() = runBlocking {
        val api = FakeDiplomaApi(
            response = VacancyResponseDto(
                found = -1,
                pages = -2,
                page = 1,
                items = emptyList(),
            ),
        )
        val repository = RetrofitVacancyRepository(api, Dispatchers.Unconfined)

        val result = repository.search(VacancySearchRequest(text = "Android")).getOrThrow()

        assertEquals(0, result.found)
        assertEquals(0, result.pages)
    }

    @Test
    fun `search failure keeps original exception`() = runBlocking {
        val expected = IllegalStateException("search unavailable")
        val repository = RetrofitVacancyRepository(
            FakeDiplomaApi(failure = expected),
            Dispatchers.Unconfined,
        )

        val result = repository.search(VacancySearchRequest(text = "Android"))

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    @Test
    fun `search cancellation is rethrown`() {
        val expected = CancellationException("cancelled")
        val repository = RetrofitVacancyRepository(
            FakeDiplomaApi(failure = expected),
            Dispatchers.Unconfined,
        )

        val actual = try {
            runBlocking { repository.search(VacancySearchRequest(text = "Android")) }
            null
        } catch (exception: CancellationException) {
            exception
        }

        assertSame(expected, actual)
    }

    @Test
    fun `fatal errors are not converted to result failure`() {
        val expected = AssertionError("fatal")
        val repository = RetrofitVacancyRepository(
            FakeDiplomaApi(failure = expected),
            Dispatchers.Unconfined,
        )

        val actual = try {
            runBlocking { repository.search(VacancySearchRequest(text = "Android")) }
            null
        } catch (error: AssertionError) {
            error
        }

        assertSame(expected, actual)
    }

}

private class FakeDiplomaApi(
    private val response: VacancyResponseDto = VacancyResponseDto(
        found = null,
        pages = null,
        page = null,
        items = null,
    ),
    private val failure: Throwable? = null,
) : DiplomaApi {
    var lastCall: CapturedSearchCall? = null

    override suspend fun getAreas(): List<AreaDto> = error("Areas are not used by search tests")

    override suspend fun getIndustries(): List<IndustryDto> = error("Industries are not used by search tests")

    override suspend fun getVacancy(id: String): VacancyDetailResponseDto = error("Vacancy detail is not used by search tests")

    override suspend fun searchVacancies(
        text: String,
        area: Int?,
        industry: Int?,
        salary: Int?,
        page: Int?,
        onlyWithSalary: Boolean?,
    ): VacancyResponseDto {
        lastCall = CapturedSearchCall(
            text = text,
            area = area,
            industry = industry,
            salary = salary,
            page = page,
            onlyWithSalary = onlyWithSalary,
        )
        failure?.let { throwable -> throw throwable }
        return response
    }
}

private data class CapturedSearchCall(
    val text: String,
    val area: Int?,
    val industry: Int?,
    val salary: Int?,
    val page: Int?,
    val onlyWithSalary: Boolean?,
)

private fun VacancySearchRequest.toCapturedSearchCall(): CapturedSearchCall = CapturedSearchCall(
    text = text,
    area = area,
    industry = industry,
    salary = salary,
    page = page,
    onlyWithSalary = onlyWithSalary,
)

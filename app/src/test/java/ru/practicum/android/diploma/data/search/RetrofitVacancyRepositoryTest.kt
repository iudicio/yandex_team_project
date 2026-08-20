package ru.practicum.android.diploma.data.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.data.search.dto.VacancyCardDto
import ru.practicum.android.diploma.data.search.dto.VacancySalaryDto
import ru.practicum.android.diploma.data.search.dto.VacancySearchResponseDto
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import java.io.IOException

class RetrofitVacancyRepositoryTest {

    @Test
    fun `passes trimmed nonblank query and first page then maps valid cards`() = runBlocking {
        val api = FakeSearchApi(
            response = response(
                items = listOf(
                    card(id = "1", name = "Android", company = "Company"),
                    card(id = " ", name = "Invalid"),
                    null,
                ),
            ),
        )
        val repository = RetrofitVacancyRepository(api, Dispatchers.Unconfined)

        val outcome = repository.search(VacancySearchRequest(text = "  Android  "))

        assertEquals(CapturedRequest("Android", 1), api.request)
        val page = (outcome as SearchOutcome.Success).value
        assertEquals(5, page.found)
        assertEquals(3, page.pages)
        assertEquals(1, page.page)
        assertEquals(listOf("1"), page.items.map { item -> item.id })
        assertEquals("Company", page.items.single().company)
        assertEquals(100_000, page.items.single().salary?.from)
    }

    @Test
    fun `nullable response fields become safe values`() = runBlocking {
        val api = FakeSearchApi(VacancySearchResponseDto(null, null, null, null))

        val page = (RetrofitVacancyRepository(api, Dispatchers.Unconfined)
            .search(VacancySearchRequest("Android", page = 4)) as SearchOutcome.Success).value

        assertEquals(0, page.found)
        assertEquals(0, page.pages)
        assertEquals(4, page.page)
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `only explicit no internet exception becomes no internet`() = runBlocking {
        val offline = RetrofitVacancyRepository(
            FakeSearchApi(failure = NoInternetException()),
            Dispatchers.Unconfined,
        )
        val otherIo = RetrofitVacancyRepository(
            FakeSearchApi(failure = IOException("timeout")),
            Dispatchers.Unconfined,
        )

        assertEquals(
            SearchOutcome.Failure(SearchError.NoInternet),
            offline.search(VacancySearchRequest("Android")),
        )
        assertEquals(
            SearchOutcome.Failure(SearchError.Generic),
            otherIo.search(VacancySearchRequest("Android")),
        )
    }

    @Test
    fun `generic exception becomes feature generic error`() = runBlocking {
        val repository = RetrofitVacancyRepository(
            FakeSearchApi(failure = IllegalStateException("server")),
            Dispatchers.Unconfined,
        )

        assertEquals(
            SearchOutcome.Failure(SearchError.Generic),
            repository.search(VacancySearchRequest("Android")),
        )
    }

    @Test
    fun `cancellation is rethrown`() {
        val expected = CancellationException("cancel")
        val repository = RetrofitVacancyRepository(FakeSearchApi(failure = expected), Dispatchers.Unconfined)

        val actual = try {
            runBlocking { repository.search(VacancySearchRequest("Android")) }
            null
        } catch (exception: CancellationException) {
            exception
        }

        assertSame(expected, actual)
    }

    @Test
    fun `blank fields in otherwise valid card are omitted`() = runBlocking {
        val repository = RetrofitVacancyRepository(
            FakeSearchApi(response(items = listOf(card(company = " ", city = "", logo = " ")))),
            Dispatchers.Unconfined,
        )

        val card = (repository.search(VacancySearchRequest("Android")) as SearchOutcome.Success)
            .value.items.single()

        assertNull(card.company)
        assertNull(card.city)
        assertNull(card.logo)
    }
}

private class FakeSearchApi(
    private val response: VacancySearchResponseDto = response(),
    private val failure: Throwable? = null,
) : VacancySearchApi {
    var request: CapturedRequest? = null

    override suspend fun searchVacancies(text: String, page: Int): VacancySearchResponseDto {
        request = CapturedRequest(text, page)
        failure?.let { throwable -> throw throwable }
        return response
    }
}

private data class CapturedRequest(val text: String, val page: Int)

private fun response(
    found: Int? = 5,
    pages: Int? = 3,
    page: Int? = 1,
    items: List<VacancyCardDto?>? = listOf(card()),
): VacancySearchResponseDto = VacancySearchResponseDto(found, pages, page, items)

private fun card(
    id: String? = "1",
    name: String? = "Android developer",
    company: String? = null,
    city: String? = null,
    logo: String? = null,
): VacancyCardDto = VacancyCardDto(
    id = id,
    name = name,
    company = company,
    city = city,
    salary = VacancySalaryDto(from = 100_000, to = 150_000, currency = "RUR"),
    logo = logo,
)

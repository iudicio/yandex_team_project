package ru.practicum.android.diploma.data.filter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.practicum.android.diploma.data.filter.dto.IndustryDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.Industry

class RetrofitIndustriesRepositoryTest {
    @Test
    fun `maps flat and legacy nested industries with string ids`() = runBlocking {
        val api = FakeIndustriesApi(
            response = listOf(
                IndustryDto(
                    id = " 7 ",
                    name = " IT ",
                    industries = listOf(IndustryDto("7.540", " Разработка ПО ")),
                ),
                IndustryDto("7.540", "duplicate"),
                IndustryDto("", "invalid"),
                null,
            ),
        )

        val outcome = RetrofitIndustriesRepository(api, Dispatchers.Unconfined).loadIndustries()

        assertEquals(
            listOf(Industry("7", "IT"), Industry("7.540", "Разработка ПО")),
            (outcome as FilterCatalogOutcome.Success).value,
        )
    }

    @Test
    fun `maps network server and generic errors`() = runBlocking {
        val offline = RetrofitIndustriesRepository(
            FakeIndustriesApi(failure = NoInternetException()),
            Dispatchers.Unconfined,
        )
        val server = RetrofitIndustriesRepository(
            FakeIndustriesApi(failure = httpException(503)),
            Dispatchers.Unconfined,
        )
        val generic = RetrofitIndustriesRepository(
            FakeIndustriesApi(failure = IllegalStateException("server")),
            Dispatchers.Unconfined,
        )

        assertEquals(
            FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet),
            offline.loadIndustries(),
        )
        assertEquals(
            FilterCatalogOutcome.Failure(FilterCatalogError.Server),
            server.loadIndustries(),
        )
        assertEquals(
            FilterCatalogOutcome.Failure(FilterCatalogError.Generic),
            generic.loadIndustries(),
        )
    }

    @Test
    fun `sorts industry names with russian collation including yo`() = runBlocking {
        val api = FakeIndustriesApi(
            response = listOf(
                IndustryDto("1", "Яхты"),
                IndustryDto("2", "Ёлки"),
                IndustryDto("3", "Животные"),
            ),
        )

        val outcome = RetrofitIndustriesRepository(api, Dispatchers.Unconfined).loadIndustries()

        assertEquals(
            listOf("2", "3", "1"),
            (outcome as FilterCatalogOutcome.Success).value.map(Industry::id),
        )
    }

    @Test
    fun `rethrows cancellation`() {
        val expected = CancellationException("cancel")
        val repository = RetrofitIndustriesRepository(
            FakeIndustriesApi(failure = expected),
            Dispatchers.Unconfined,
        )

        val actual = try {
            runBlocking { repository.loadIndustries() }
            null
        } catch (exception: CancellationException) {
            exception
        }

        assertSame(expected, actual)
    }
}

private fun httpException(code: Int): HttpException = HttpException(
    Response.error<Any>(
        code,
        "{}".toResponseBody("application/json".toMediaType()),
    ),
)

private class FakeIndustriesApi(
    private val response: List<IndustryDto?> = emptyList(),
    private val failure: Throwable? = null,
) : IndustriesApi {
    override suspend fun getIndustries(): List<IndustryDto?> {
        failure?.let { throwable -> throw throwable }
        return response
    }
}

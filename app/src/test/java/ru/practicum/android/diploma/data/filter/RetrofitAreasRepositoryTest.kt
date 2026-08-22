package ru.practicum.android.diploma.data.filter

import com.google.gson.Gson
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
import ru.practicum.android.diploma.data.filter.dto.AreaDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome

class RetrofitAreasRepositoryTest {
    @Test
    fun `maps hierarchy and supplies missing child parent id`() = runBlocking {
        val repository = RetrofitAreasRepository(
            FakeAreasApi(
                listOf(
                    AreaDto(
                        id = 113,
                        name = " Россия ",
                        areas = listOf(
                            AreaDto(1, " Москва "),
                            AreaDto(id = null, name = "invalid"),
                        ),
                    ),
                    null,
                ),
            ),
            Dispatchers.Unconfined,
        )

        val areas = (repository.loadAreas() as FilterCatalogOutcome.Success).value

        assertEquals(listOf(Area(113, "Россия", areas = listOf(Area(1, "Москва", 113)))), areas)
    }

    @Test
    fun `deserializes both camel and snake case parent id`() {
        val gson = Gson()
        val camelCase = gson.fromJson(
            "{\"id\":1,\"name\":\"Москва\",\"parentId\":113}",
            AreaDto::class.java,
        )
        val snakeCase = gson.fromJson(
            "{\"id\":1,\"name\":\"Москва\",\"parent_id\":113}",
            AreaDto::class.java,
        )

        assertEquals(113, camelCase.parentId)
        assertEquals(113, snakeCase.parentId)
    }

    @Test
    fun `maps network server and generic errors and rethrows cancellation`() = runBlocking {
        val offline = RetrofitAreasRepository(FakeAreasApi(failure = NoInternetException()), Dispatchers.Unconfined)
        val server = RetrofitAreasRepository(FakeAreasApi(failure = httpException(503)), Dispatchers.Unconfined)
        val generic = RetrofitAreasRepository(
            FakeAreasApi(failure = IllegalStateException("server")),
            Dispatchers.Unconfined,
        )
        val cancellation = CancellationException("cancel")
        val cancelled = RetrofitAreasRepository(FakeAreasApi(failure = cancellation), Dispatchers.Unconfined)

        assertEquals(FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet), offline.loadAreas())
        assertEquals(FilterCatalogOutcome.Failure(FilterCatalogError.Server), server.loadAreas())
        assertEquals(FilterCatalogOutcome.Failure(FilterCatalogError.Generic), generic.loadAreas())
        val actual = try {
            cancelled.loadAreas()
            null
        } catch (exception: CancellationException) {
            exception
        }
        assertSame(cancellation, actual)
    }
}

private fun httpException(code: Int): HttpException = HttpException(
    Response.error<Any>(
        code,
        "{}".toResponseBody("application/json".toMediaType()),
    ),
)

private class FakeAreasApi(
    private val response: List<AreaDto?> = emptyList(),
    private val failure: Throwable? = null,
) : AreasApi {
    override suspend fun getAreas(): List<AreaDto?> {
        failure?.let { throwable -> throw throwable }
        return response
    }
}

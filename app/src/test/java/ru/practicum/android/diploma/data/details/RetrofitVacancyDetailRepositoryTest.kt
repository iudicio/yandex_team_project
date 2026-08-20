package ru.practicum.android.diploma.data.details

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.details.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.details.VacancyDetailsError
import ru.practicum.android.diploma.domain.details.VacancyDetailsOutcome
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitVacancyDetailRepositoryTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = TestDispatcherProvider(dispatcher)

    @Test
    fun `successful api response maps content and forwards trimmed id`() = runTest(dispatcher) {
        val api = FakeDetailsApi {
            VacancyDetailResponseDto(id = null, name = "Vacancy")
        }
        val repository = RetrofitVacancyDetailRepository(api, dispatcherProvider)

        val outcome = repository.getVacancy(" 123 ") as VacancyDetailsOutcome.Content

        assertEquals("123", api.requestedId)
        assertEquals("123", outcome.vacancy.id)
        assertEquals("Vacancy", outcome.vacancy.name)
        assertEquals(false, outcome.isOffline)
    }

    @Test
    fun `connectivity interceptor failure maps to feature no internet error`() = runTest(dispatcher) {
        val repository = repositoryThrowing(NoInternetException())

        assertEquals(
            VacancyDetailsOutcome.Failure(VacancyDetailsError.NoInternet),
            repository.getVacancy("1"),
        )
    }

    @Test
    fun `transport IOException is a server error rather than an offline signal`() = runTest(dispatcher) {
        val repository = repositoryThrowing(IOException("timeout or DNS failure"))

        assertEquals(
            VacancyDetailsOutcome.Failure(VacancyDetailsError.Server),
            repository.getVacancy("1"),
        )
    }

    @Test
    fun `http 404 maps to feature not found error`() = runTest(dispatcher) {
        val repository = repositoryThrowing(httpException(404))

        assertEquals(
            VacancyDetailsOutcome.Failure(VacancyDetailsError.NotFound),
            repository.getVacancy("1"),
        )
    }

    @Test
    fun `server and unexpected failures use server error`() = runTest(dispatcher) {
        val serverRepository = repositoryThrowing(httpException(500))
        val unexpectedRepository = repositoryThrowing(IllegalStateException("bad json"))
        val expected = VacancyDetailsOutcome.Failure(VacancyDetailsError.Server)

        assertEquals(expected, serverRepository.getVacancy("1"))
        assertEquals(expected, unexpectedRepository.getVacancy("1"))
    }

    @Test
    fun `cancellation is never converted into recoverable outcome`() = runTest(dispatcher) {
        val cancellation = CancellationException("cancelled")
        val repository = repositoryThrowing(cancellation)

        val thrown = try {
            repository.getVacancy("1")
            throw AssertionError("CancellationException was expected")
        } catch (exception: CancellationException) {
            exception
        }

        assertEquals(cancellation.message, thrown.message)
    }

    private fun repositoryThrowing(throwable: Throwable): RetrofitVacancyDetailRepository =
        RetrofitVacancyDetailRepository(
            api = FakeDetailsApi { throw throwable },
            dispatcherProvider = dispatcherProvider,
        )
}

private class FakeDetailsApi(
    private val response: suspend () -> VacancyDetailResponseDto,
) : VacancyDetailsApi {
    var requestedId: String? = null

    override suspend fun getVacancy(vacancyId: String): VacancyDetailResponseDto {
        requestedId = vacancyId
        return response()
    }
}

private class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}

private fun httpException(code: Int): HttpException = HttpException(
    Response.error<Any>(code, "error".toResponseBody()),
)

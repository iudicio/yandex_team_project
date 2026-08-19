package ru.practicum.android.diploma.data.common

import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.common.AppError
import java.net.SocketTimeoutException

class AppErrorMapperTest {

    @Test
    fun `maps unavailable network to a typed error`() {
        assertEquals(AppError.NoInternet, AppErrorMapper.map(NoInternetException()))
    }

    @Test
    fun `maps timeout to a typed error`() {
        assertEquals(AppError.NoInternet, AppErrorMapper.map(SocketTimeoutException()))
    }

    @Test
    fun `maps authorization statuses to unauthorized`() {
        assertEquals(AppError.Unauthorized, AppErrorMapper.map(httpException(HTTP_UNAUTHORIZED)))
        assertEquals(AppError.Unauthorized, AppErrorMapper.map(httpException(HTTP_FORBIDDEN)))
    }

    @Test
    fun `maps not found status to not found`() {
        assertEquals(AppError.NotFound, AppErrorMapper.map(httpException(HTTP_NOT_FOUND)))
    }

    @Test
    fun `maps server status to server error`() {
        val exception = httpException(SERVICE_UNAVAILABLE)

        assertEquals(AppError.Server(SERVICE_UNAVAILABLE), AppErrorMapper.map(exception))
    }

    @Test
    fun `preserves an unexpected exception`() {
        val exception = IllegalStateException("Unexpected state")
        val error = AppErrorMapper.map(exception) as AppError.Unknown

        assertSame(exception, error.cause)
    }

    @Test
    fun `does not consume coroutine cancellation`() {
        val cancellation = CancellationException("Cancelled")

        val thrown = assertThrows(CancellationException::class.java) {
            AppErrorMapper.map(cancellation)
        }

        assertSame(cancellation, thrown)
    }

    private fun httpException(statusCode: Int): HttpException {
        val responseBody = "Error".toResponseBody("text/plain".toMediaType())
        return HttpException(Response.error<Unit>(statusCode, responseBody))
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val SERVICE_UNAVAILABLE = 503
    }
}

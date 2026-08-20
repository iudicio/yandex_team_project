package ru.practicum.android.diploma.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class NetworkAvailabilityInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `blocks request when network is unavailable`() {
        val client = client(isConnected = false)

        assertThrows(NoInternetException::class.java) {
            client.newCall(request()).execute()
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `continues request when network is available`() {
        server.enqueue(MockResponse().setResponseCode(HTTP_OK))
        val client = client(isConnected = true)

        client.newCall(request()).execute().use { response ->
            assertEquals(HTTP_OK, response.code)
        }
        assertEquals(1, server.requestCount)
    }

    private fun client(isConnected: Boolean): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            NetworkAvailabilityInterceptor(
                connectivityChecker = ConnectivityChecker { isConnected },
            ),
        )
        .build()

    private fun request(): Request = Request.Builder()
        .url(server.url("/health"))
        .build()

    private companion object {
        const val HTTP_OK = 200
    }
}

package ru.practicum.android.diploma.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthorizationInterceptorTest {

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
    fun `adds trimmed bearer token to every request`() {
        server.enqueue(MockResponse().setResponseCode(HTTP_OK))
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor("  secret-token  "))
            .build()
        val request = Request.Builder()
            .url(server.url("/vacancies"))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(HTTP_OK, response.code)
        }

        assertEquals("Bearer secret-token", server.takeRequest().getHeader(AUTHORIZATION_HEADER))
    }

    @Test
    fun `does not add authorization header when token is blank`() {
        server.enqueue(MockResponse().setResponseCode(HTTP_OK))
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor("   "))
            .build()
        val request = Request.Builder()
            .url(server.url("/vacancies"))
            .build()

        client.newCall(request).execute().close()

        assertNull(server.takeRequest().getHeader(AUTHORIZATION_HEADER))
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val HTTP_OK = 200
    }
}

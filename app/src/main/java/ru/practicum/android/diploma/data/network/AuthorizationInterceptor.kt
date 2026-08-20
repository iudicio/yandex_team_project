package ru.practicum.android.diploma.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(accessToken: String) : Interceptor {

    private val accessToken = accessToken.trim()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (accessToken.isEmpty()) {
            return chain.proceed(chain.request())
        }

        val authenticatedRequest = chain.request()
            .newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer $accessToken")
            .build()
        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}

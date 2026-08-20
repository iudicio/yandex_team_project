package ru.practicum.android.diploma.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NoInternetException : IOException("No active internet connection")

class NetworkAvailabilityInterceptor(
    private val connectivityChecker: ConnectivityChecker,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!connectivityChecker.isConnected()) {
            throw NoInternetException()
        }
        return chain.proceed(chain.request())
    }
}

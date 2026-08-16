package ru.practicum.android.diploma.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val BASE_URL = "https://android-diploma.education-services.ru/"

class AuthorizationInterceptor(
    accessToken: String,
) : Interceptor {

    private val authorizationValue = "Bearer ${accessToken.trim()}"

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request()
            .newBuilder()
            .header(AUTHORIZATION_HEADER, authorizationValue)
            .build(),
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}

class NoInternetException : IOException("No active network connection")

class NetworkAvailabilityInterceptor(
    private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw NoInternetException()
        }
        return chain.proceed(chain.request())
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        val transports = listOf(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_ETHERNET,
        )
        return transports.any { transport -> capabilities.hasTransport(transport) }
    }
}

object NetworkClientFactory {

    fun createApi(context: Context, accessToken: String): DiplomaApi {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(NetworkAvailabilityInterceptor(context))
            .addInterceptor(AuthorizationInterceptor(accessToken))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiplomaApi::class.java)
    }
}

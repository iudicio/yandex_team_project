package ru.practicum.android.diploma.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://android-diploma.education-services.ru/"

class AuthorizationInterceptor(
    accessToken: String,
) : Interceptor {

    private val authorizationValue = "Bearer ${accessToken.trim()}"

    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request()
            .newBuilder()
            .header(AUTHORIZATION_HEADER, authorizationValue)
            .build(),
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}

object NetworkClientFactory {
    fun createApi(accessToken: String): DiplomaApi {
        val httpClient = OkHttpClient.Builder()
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

package ru.practicum.android.diploma.data.network

import retrofit2.Retrofit

class NetworkClient(private val retrofit: Retrofit) {

    fun <T : Any> create(service: Class<T>): T = retrofit.create(service)
}

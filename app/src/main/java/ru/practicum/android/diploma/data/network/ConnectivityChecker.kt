package ru.practicum.android.diploma.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun interface ConnectivityChecker {
    fun isConnected(): Boolean
}

class AndroidConnectivityChecker(context: Context) : ConnectivityChecker {

    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override fun isConnected(): Boolean {
        val capabilities = connectivityManager?.run {
            activeNetwork?.let { network -> getNetworkCapabilities(network) }
        }
        return capabilities?.let { networkCapabilities ->
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
    }
}

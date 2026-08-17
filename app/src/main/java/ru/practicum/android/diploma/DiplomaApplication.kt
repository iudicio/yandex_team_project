package ru.practicum.android.diploma

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.practicum.android.diploma.di.appModules

class DiplomaApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DiplomaApplication)
            modules(appModules)
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient))
                add(SvgDecoder.Factory())
            }
            .build()
    }
}

private class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/115.0.0.0 Safari/537.36"
            )
            .build()
        return chain.proceed(newRequest)
    }
}

package ru.practicum.android.diploma.di

import androidx.room.Room
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.common.coroutines.DefaultDispatcherProvider
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.network.AndroidConnectivityChecker
import ru.practicum.android.diploma.data.network.AuthorizationInterceptor
import ru.practicum.android.diploma.data.network.ConnectivityChecker
import ru.practicum.android.diploma.data.network.NetworkAvailabilityInterceptor
import ru.practicum.android.diploma.data.network.RetrofitFactory
import java.util.concurrent.TimeUnit

private const val DATABASE_NAME = "diploma.db"
private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val IO_TIMEOUT_SECONDS = 30L
private const val CALL_TIMEOUT_SECONDS = 45L

private val commonModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

private val networkModule = module {
    single<ConnectivityChecker> { AndroidConnectivityChecker(androidContext()) }
    single { AuthorizationInterceptor(BuildConfig.API_ACCESS_TOKEN) }
    single { NetworkAvailabilityInterceptor(get()) }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<NetworkAvailabilityInterceptor>())
            .addInterceptor(get<AuthorizationInterceptor>())
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single { RetrofitFactory.create(get()) }
}

private val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = DATABASE_NAME,
        ).build()
    }
    single { get<AppDatabase>().favoriteVacancyDao() }
}

val appModules: List<Module> = listOf(commonModule, networkModule, databaseModule)

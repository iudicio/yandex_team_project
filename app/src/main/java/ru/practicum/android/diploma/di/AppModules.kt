package ru.practicum.android.diploma.di

import androidx.room.Room
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.common.coroutines.DefaultDispatcherProvider
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.details.RetrofitVacancyDetailRepository
import ru.practicum.android.diploma.data.details.VacancyDetailsApi
import ru.practicum.android.diploma.data.favorites.RoomFavoriteVacancyRepository
import ru.practicum.android.diploma.data.network.AndroidConnectivityChecker
import ru.practicum.android.diploma.data.network.AuthorizationInterceptor
import ru.practicum.android.diploma.data.network.ConnectivityChecker
import ru.practicum.android.diploma.data.network.NetworkAvailabilityInterceptor
import ru.practicum.android.diploma.data.network.RetrofitFactory
import ru.practicum.android.diploma.data.search.RetrofitVacancyRepository
import ru.practicum.android.diploma.data.search.VacancySearchApi
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractor
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractorImpl
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractorImpl
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractor
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.presentation.search.SearchViewModel
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
    single<VacancySearchApi> { get<Retrofit>().create(VacancySearchApi::class.java) }
    single<VacancyDetailsApi> { get<Retrofit>().create(VacancyDetailsApi::class.java) }
}

private val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = DATABASE_NAME,
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }
    single { get<AppDatabase>().favoriteVacancyDao() }
}

private val dataModule = module {
    single<VacancyRepository> {
        RetrofitVacancyRepository(
            api = get(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }
    single<FavoriteVacancyRepository> { RoomFavoriteVacancyRepository(get(), get()) }
    single<VacancyDetailRepository> { RetrofitVacancyDetailRepository(get(), get()) }
}

private val domainModule = module {
    factory<SearchVacanciesInteractor> { SearchVacanciesInteractorImpl(get()) }
    factory<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
    factory<VacancyDetailsInteractor> { VacancyDetailsInteractorImpl(get(), get()) }
}

private val presentationModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { parameters ->
        DetailsViewModel(
            vacancyId = parameters.get(),
            vacancyDetailsInteractor = get(),
            favoritesInteractor = get(),
        )
    }
}

val appModules: List<Module> = listOf(
    commonModule,
    networkModule,
    databaseModule,
    dataModule,
    domainModule,
    presentationModule,
)

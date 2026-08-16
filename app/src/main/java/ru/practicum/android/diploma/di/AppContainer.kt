package ru.practicum.android.diploma.di

import android.content.Context
import androidx.room.Room
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.DiplomaApplication
import ru.practicum.android.diploma.data.favorites.FavoriteVacancyDatabase
import ru.practicum.android.diploma.data.favorites.RoomFavoriteVacancyRepository
import ru.practicum.android.diploma.data.filter.FILTER_SETTINGS_PREFERENCES_NAME
import ru.practicum.android.diploma.data.filter.RetrofitCatalogRepository
import ru.practicum.android.diploma.data.filter.SharedPreferencesFilterSettingsRepository
import ru.practicum.android.diploma.data.network.NetworkClientFactory
import ru.practicum.android.diploma.data.search.RetrofitVacancyRepository
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractorImpl
import ru.practicum.android.diploma.domain.filter.CatalogRepository
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractor
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl
import ru.practicum.android.diploma.domain.search.VacancyRepository

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext
    private val api by lazy {
        NetworkClientFactory.createApi(applicationContext, BuildConfig.API_ACCESS_TOKEN)
    }

    val filterSettingsRepository: FilterSettingsRepository by lazy {
        SharedPreferencesFilterSettingsRepository(
            applicationContext.getSharedPreferences(
                FILTER_SETTINGS_PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            ),
        )
    }

    val catalogRepository: CatalogRepository by lazy {
        RetrofitCatalogRepository(api)
    }

    val vacancyRepository: VacancyRepository by lazy {
        RetrofitVacancyRepository(api)
    }

    private val favoritesDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FavoriteVacancyDatabase::class.java,
            FavoriteVacancyDatabase.DATABASE_NAME,
        ).build()
    }

    val favoriteVacancyRepository: FavoriteVacancyRepository by lazy {
        RoomFavoriteVacancyRepository(favoritesDatabase.favoriteVacancyDao())
    }

    val searchInteractor: SearchVacanciesInteractor by lazy {
        SearchVacanciesInteractorImpl(vacancyRepository)
    }

    val favoritesInteractor: FavoritesInteractor by lazy {
        FavoritesInteractorImpl(favoriteVacancyRepository)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as DiplomaApplication).appContainer

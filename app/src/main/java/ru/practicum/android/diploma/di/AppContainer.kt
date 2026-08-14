package ru.practicum.android.diploma.di

import android.content.Context
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.DiplomaApplication
import ru.practicum.android.diploma.data.filter.FILTER_SETTINGS_PREFERENCES_NAME
import ru.practicum.android.diploma.data.filter.RetrofitCatalogRepository
import ru.practicum.android.diploma.data.filter.SharedPreferencesFilterSettingsRepository
import ru.practicum.android.diploma.data.network.NetworkClientFactory
import ru.practicum.android.diploma.data.search.RetrofitVacancyRepository
import ru.practicum.android.diploma.domain.filter.CatalogRepository
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.VacancyRepository

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext
    private val api by lazy {
        NetworkClientFactory.createApi(BuildConfig.API_ACCESS_TOKEN)
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
}

val Context.appContainer: AppContainer
    get() = (applicationContext as DiplomaApplication).appContainer

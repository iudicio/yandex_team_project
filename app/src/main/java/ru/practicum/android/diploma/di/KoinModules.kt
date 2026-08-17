package ru.practicum.android.diploma.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.details.RetrofitVacancyDetailRepository
import ru.practicum.android.diploma.data.favorites.RoomFavoriteVacancyRepository
import ru.practicum.android.diploma.data.filter.FILTER_SETTINGS_PREFERENCES_NAME
import ru.practicum.android.diploma.data.filter.RetrofitCatalogRepository
import ru.practicum.android.diploma.data.filter.SharedPreferencesFilterSettingsRepository
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.NetworkClientFactory
import ru.practicum.android.diploma.data.search.RetrofitVacancyRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractor
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractorImpl
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractorImpl
import ru.practicum.android.diploma.domain.filter.CatalogRepository
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractor
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractorImpl
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.presentation.filter.CountryViewModel
import ru.practicum.android.diploma.presentation.filter.FilterViewModel
import ru.practicum.android.diploma.presentation.filter.IndustryViewModel
import ru.practicum.android.diploma.presentation.filter.RegionViewModel
import ru.practicum.android.diploma.presentation.filter.WorkplaceViewModel
import ru.practicum.android.diploma.presentation.search.SearchViewModel

val dataModule = module {
    single<DiplomaApi> {
        NetworkClientFactory.createApi(androidContext(), BuildConfig.API_ACCESS_TOKEN)
    }
    single { Gson() }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "diploma_database").build()
    }
    single { get<AppDatabase>().favoriteVacancyDao() }
    single<FilterSettingsRepository> {
        SharedPreferencesFilterSettingsRepository(
            androidContext().getSharedPreferences(FILTER_SETTINGS_PREFERENCES_NAME, Context.MODE_PRIVATE),
        )
    }
    single<CatalogRepository> { RetrofitCatalogRepository(get()) }
    single<VacancyRepository> { RetrofitVacancyRepository(get()) }
    single<VacancyDetailRepository> { RetrofitVacancyDetailRepository(get(), get(), get()) }
    single<FavoriteVacancyRepository> { RoomFavoriteVacancyRepository(get(), get()) }
}

val domainModule = module {
    factory<SearchVacanciesInteractor> { SearchVacanciesInteractorImpl(get()) }
    factory<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
    factory<VacancyDetailsInteractor> { VacancyDetailsInteractorImpl(get()) }
}

val presentationModule = module {
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { params ->
        DetailsViewModel(
            savedStateHandle = get(),
            vacancyId = params.get(),
            vacancyDetailsInteractor = get(),
            favoritesInteractor = get(),
        )
    }
    viewModel { FilterViewModel(get()) }
    viewModel { WorkplaceViewModel(get<FilterSettingsRepository>().load()) }
    viewModel { params -> CountryViewModel(get(), params.getOrNull()) }
    viewModel { params -> RegionViewModel(get(), params.getOrNull(), params.getOrNull()) }
    viewModel { params -> IndustryViewModel(get(), params.getOrNull()) }
}

val appModules = listOf(dataModule, domainModule, presentationModule)

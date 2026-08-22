package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.AreasInteractor
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor
import ru.practicum.android.diploma.domain.filter.toSelection

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class CountryViewModel(
    private val areasInteractor: AreasInteractor,
    private val settingsInteractor: FilterSettingsInteractor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        CountryUiState(selectedCountryId = settingsInteractor.current().country?.id),
    )
    val state: StateFlow<CountryUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<CountryEvent>(Channel.BUFFERED)
    val events: Flow<CountryEvent> = eventChannel.receiveAsFlow()

    private var countries: List<Area> = emptyList()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun select(country: Area) {
        val canonicalCountry = countries.firstOrNull { item -> item.id == country.id } ?: return
        val current = settingsInteractor.current()
        val region = current.region.takeIf { current.country?.id == canonicalCountry.id }
        settingsInteractor.save(
            current.copy(country = canonicalCountry.toSelection(), region = region),
        )
        mutableState.value = mutableState.value.copy(selectedCountryId = canonicalCountry.id)
        eventChannel.trySend(CountryEvent.Selected(canonicalCountry.toSelection()))
    }

    fun back() {
        eventChannel.trySend(CountryEvent.Close)
    }

    private fun load() {
        loadJob?.cancel()
        mutableState.value = mutableState.value.copy(result = AreaResultUiState.Loading)
        loadJob = viewModelScope.launch {
            when (val outcome = safeLoad()) {
                is FilterCatalogOutcome.Success -> {
                    countries = areasInteractor.countries(outcome.value)
                    showCountries()
                }
                is FilterCatalogOutcome.Failure -> {
                    countries = emptyList()
                    mutableState.value = mutableState.value.copy(result = outcome.error.toAreaUiState())
                }
            }
        }
    }

    private fun showCountries() {
        mutableState.value = mutableState.value.copy(
            result = if (countries.isEmpty()) AreaResultUiState.Empty else AreaResultUiState.Content(countries),
        )
    }

    private suspend fun safeLoad(): FilterCatalogOutcome<List<Area>> = try {
        areasInteractor.loadAreas()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        FilterCatalogOutcome.Failure(FilterCatalogError.Generic)
    }
}

internal fun FilterCatalogError.toAreaUiState(): AreaResultUiState = when (this) {
    FilterCatalogError.NoInternet -> AreaResultUiState.NoInternet
    FilterCatalogError.Server -> AreaResultUiState.ServerError
    FilterCatalogError.Generic -> AreaResultUiState.GenericError
}

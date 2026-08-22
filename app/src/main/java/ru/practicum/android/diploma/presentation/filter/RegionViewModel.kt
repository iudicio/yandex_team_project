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
class RegionViewModel(
    private val areasInteractor: AreasInteractor,
    private val settingsInteractor: FilterSettingsInteractor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        RegionUiState(selectedRegionId = settingsInteractor.current().region?.id),
    )
    val state: StateFlow<RegionUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<RegionEvent>(Channel.BUFFERED)
    val events: Flow<RegionEvent> = eventChannel.receiveAsFlow()

    private var areaTree: List<Area> = emptyList()
    private var regions: List<Area> = emptyList()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun onQueryChanged(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
        if (regions.isNotEmpty()) showFilteredRegions()
    }

    fun clearQuery() = onQueryChanged("")

    fun retry() = load()

    fun select(region: Area) {
        val canonicalRegion = regions.firstOrNull { item -> item.id == region.id } ?: return
        val country = areasInteractor.countryForRegion(areaTree, canonicalRegion.id) ?: return
        settingsInteractor.save(
            settingsInteractor.current().copy(
                country = country.toSelection(),
                region = canonicalRegion.toSelection(),
            ),
        )
        mutableState.value = mutableState.value.copy(selectedRegionId = canonicalRegion.id)
        eventChannel.trySend(RegionEvent.Selected(canonicalRegion.toSelection()))
    }

    fun back() {
        eventChannel.trySend(RegionEvent.Close)
    }

    private fun load() {
        loadJob?.cancel()
        mutableState.value = mutableState.value.copy(result = AreaResultUiState.Loading)
        loadJob = viewModelScope.launch {
            when (val outcome = safeLoad()) {
                is FilterCatalogOutcome.Success -> {
                    areaTree = outcome.value
                    regions = areasInteractor.regions(areaTree, settingsInteractor.current().country?.id)
                    showFilteredRegions()
                }
                is FilterCatalogOutcome.Failure -> {
                    areaTree = emptyList()
                    regions = emptyList()
                    mutableState.value = mutableState.value.copy(result = outcome.error.toAreaUiState())
                }
            }
        }
    }

    private fun showFilteredRegions() {
        val query = mutableState.value.query.trim()
        val filtered = regions.filter { region -> region.name.contains(query, ignoreCase = true) }
        mutableState.value = mutableState.value.copy(
            result = if (filtered.isEmpty()) AreaResultUiState.Empty else AreaResultUiState.Content(filtered),
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

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
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor
import ru.practicum.android.diploma.domain.filter.IndustriesInteractor
import ru.practicum.android.diploma.domain.filter.Industry

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class IndustryViewModel(
    private val industriesInteractor: IndustriesInteractor,
    private val settingsInteractor: FilterSettingsInteractor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        IndustryUiState(selectedIndustry = settingsInteractor.current().industry),
    )
    val state: StateFlow<IndustryUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<IndustryEvent>(Channel.BUFFERED)
    val events: Flow<IndustryEvent> = eventChannel.receiveAsFlow()

    private var industries: List<Industry> = emptyList()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun onQueryChanged(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
        if (industries.isNotEmpty()) showFilteredIndustries()
    }

    fun clearQuery() = onQueryChanged("")

    fun retry() = load()

    fun select(industry: Industry) {
        val canonicalIndustry = industries.firstOrNull { item -> item.id == industry.id } ?: return
        mutableState.value = mutableState.value.copy(selectedIndustry = canonicalIndustry)
    }

    fun confirm() {
        val selected = mutableState.value.selectedIndustry ?: return
        settingsInteractor.save(settingsInteractor.current().copy(industry = selected))
        eventChannel.trySend(IndustryEvent.Confirmed(selected))
    }

    fun back() {
        eventChannel.trySend(IndustryEvent.Close)
    }

    private fun load() {
        loadJob?.cancel()
        mutableState.value = mutableState.value.copy(result = IndustryResultUiState.Loading)
        loadJob = viewModelScope.launch {
            when (val outcome = safeLoad()) {
                is FilterCatalogOutcome.Success -> {
                    industries = outcome.value
                    val selected = mutableState.value.selectedIndustry
                    val canonicalSelection = selected?.let { saved ->
                        industries.firstOrNull { industry -> industry.id == saved.id }
                    }
                    if (canonicalSelection != null) {
                        mutableState.value = mutableState.value.copy(selectedIndustry = canonicalSelection)
                    }
                    showFilteredIndustries()
                }
                is FilterCatalogOutcome.Failure -> {
                    industries = emptyList()
                    mutableState.value = mutableState.value.copy(
                        result = when (outcome.error) {
                            FilterCatalogError.NoInternet -> IndustryResultUiState.NoInternet
                            FilterCatalogError.Server -> IndustryResultUiState.ServerError
                            FilterCatalogError.Generic -> IndustryResultUiState.GenericError
                        },
                    )
                }
            }
        }
    }

    private fun showFilteredIndustries() {
        val query = mutableState.value.query.trim()
        val filtered = industries.filter { industry -> industry.name.contains(query, ignoreCase = true) }
        mutableState.value = mutableState.value.copy(
            result = if (filtered.isEmpty()) {
                IndustryResultUiState.Empty
            } else {
                IndustryResultUiState.Content(filtered)
            },
        )
    }

    private suspend fun safeLoad(): FilterCatalogOutcome<List<Industry>> = try {
        industriesInteractor.loadIndustries()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        FilterCatalogOutcome.Failure(FilterCatalogError.Generic)
    }
}

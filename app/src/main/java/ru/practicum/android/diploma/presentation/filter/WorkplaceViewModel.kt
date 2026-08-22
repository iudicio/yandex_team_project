package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor

class WorkplaceViewModel(
    private val settingsInteractor: FilterSettingsInteractor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(settingsInteractor.current().toWorkplaceUiState())
    val state: StateFlow<WorkplaceUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<WorkplaceEvent>(Channel.BUFFERED)
    val events: Flow<WorkplaceEvent> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observe().collect { settings ->
                mutableState.value = settings.toWorkplaceUiState()
            }
        }
    }

    fun clearCountry() {
        settingsInteractor.save(settingsInteractor.current().copy(country = null, region = null))
    }

    fun clearRegion() {
        settingsInteractor.save(settingsInteractor.current().copy(region = null))
    }

    fun confirm() {
        val settings = settingsInteractor.current()
        eventChannel.trySend(WorkplaceEvent.Confirmed(settings.country, settings.region))
    }

    fun back() {
        eventChannel.trySend(WorkplaceEvent.Close)
    }
}

private fun ru.practicum.android.diploma.domain.filter.FilterSettings.toWorkplaceUiState(): WorkplaceUiState =
    WorkplaceUiState(country = country, region = region)

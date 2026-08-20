package ru.practicum.android.diploma.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.details.VacancyDetailsError
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractor
import ru.practicum.android.diploma.domain.details.VacancyDetailsOutcome
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor

sealed interface DetailsEvent {
    data object NavigateBack : DetailsEvent

    data class ShareVacancy(val vacancy: VacancyDetailResult) : DetailsEvent

    data class DialPhone(val phone: String) : DetailsEvent

    data class SendEmail(val email: String) : DetailsEvent

    data object FavoriteUpdateFailed : DetailsEvent
}

class DetailsViewModel(
    private val vacancyId: String,
    private val vacancyDetailsInteractor: VacancyDetailsInteractor,
    private val favoritesInteractor: FavoritesInteractor,
) : ViewModel() {

    private val mutableState = MutableStateFlow(DetailsUiState())
    val state: StateFlow<DetailsUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<DetailsEvent>(capacity = Channel.BUFFERED)
    val events: Flow<DetailsEvent> = eventChannel.receiveAsFlow()

    private val favoriteToggleMutex = Mutex()
    private var loadJob: Job? = null
    private var ignoreFavoriteObservation = false

    init {
        require(vacancyId.isNotBlank()) { "Vacancy id must not be blank" }
        observeFavoriteStatus()
        loadVacancy()
    }

    fun loadVacancy() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.update { current ->
                current.copy(
                    isLoading = true,
                    vacancy = null,
                    isOffline = false,
                    error = null,
                )
            }
            when (val outcome = vacancyDetailsInteractor.getDetails(vacancyId)) {
                is VacancyDetailsOutcome.Content -> showContent(outcome)
                is VacancyDetailsOutcome.Failure -> showError(outcome.error)
            }
        }
    }

    fun retry() {
        loadVacancy()
    }

    fun onBackPressed() {
        eventChannel.trySend(DetailsEvent.NavigateBack)
    }

    fun onShareClicked() {
        val vacancy = mutableState.value.vacancy?.takeIf { it.url.isNotBlank() } ?: return
        eventChannel.trySend(DetailsEvent.ShareVacancy(vacancy))
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            favoriteToggleMutex.withLock {
                toggleFavorite()
            }
        }
    }

    fun onPhoneClicked(phone: String) {
        phone.trim().takeIf(String::isNotEmpty)?.let { value ->
            eventChannel.trySend(DetailsEvent.DialPhone(value))
        }
    }

    fun onEmailClicked(email: String) {
        email.trim().takeIf(String::isNotEmpty)?.let { value ->
            eventChannel.trySend(DetailsEvent.SendEmail(value))
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesInteractor.observeIsFavorite(vacancyId)
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                }
                .collect { isFavorite ->
                    val isDeletedVacancy = mutableState.value.error == DetailsError.NotFound
                    if (!ignoreFavoriteObservation && !isDeletedVacancy) {
                        mutableState.update { current -> current.copy(isFavorite = isFavorite) }
                    }
                }
        }
    }

    private fun showContent(outcome: VacancyDetailsOutcome.Content) {
        mutableState.update { current ->
            current.copy(
                isLoading = false,
                vacancy = outcome.vacancy,
                isOffline = outcome.isOffline,
                error = null,
            )
        }
    }

    private fun showError(error: VacancyDetailsError) {
        mutableState.update { current ->
            current.copy(
                isLoading = false,
                vacancy = null,
                isOffline = false,
                isFavorite = if (error == VacancyDetailsError.NotFound) false else current.isFavorite,
                error = error.toUiError(),
            )
        }
    }

    private suspend fun toggleFavorite() {
        val vacancy = mutableState.value.vacancy ?: return
        val previousState = mutableState.value.isFavorite
        val targetState = !previousState
        ignoreFavoriteObservation = true
        mutableState.update { current -> current.copy(isFavorite = targetState) }

        try {
            if (targetState) {
                favoritesInteractor.add(vacancy)
            } else {
                favoritesInteractor.remove(vacancyId)
            }
        } catch (exception: CancellationException) {
            mutableState.update { current -> current.copy(isFavorite = previousState) }
            throw exception
        } catch (_: Exception) {
            mutableState.update { current -> current.copy(isFavorite = previousState) }
            eventChannel.send(DetailsEvent.FavoriteUpdateFailed)
        } finally {
            ignoreFavoriteObservation = false
        }
    }
}

private fun VacancyDetailsError.toUiError(): DetailsError = when (this) {
    VacancyDetailsError.NoInternet -> DetailsError.NoInternet
    VacancyDetailsError.NotFound -> DetailsError.NotFound
    VacancyDetailsError.Server -> DetailsError.Server
}

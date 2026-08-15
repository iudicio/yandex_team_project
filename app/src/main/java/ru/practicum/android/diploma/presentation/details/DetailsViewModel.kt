package ru.practicum.android.diploma.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.details.FavoritesRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailResult

sealed class DetailsEvent {
    object NavigateBack : DetailsEvent()
    data class ShareVacancy(val vacancy: VacancyDetailResult) : DetailsEvent()
    data class DialPhone(val phone: String) : DetailsEvent()
    data class SendEmail(val email: String) : DetailsEvent()
}

class DetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val vacancyId: String,
    private val vacancyDetailRepository: VacancyDetailRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsUiState())
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DetailsEvent>()
    val events: SharedFlow<DetailsEvent> = _events.asSharedFlow()

    init {
        loadVacancy()
        checkFavoriteStatus()
    }

    fun loadVacancy() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            vacancyDetailRepository.getVacancy(vacancyId)
                .onSuccess { vacancy ->
                    _state.value = DetailsUiState(
                        isLoading = false,
                        vacancy = vacancy,
                        isFavorite = _state.value.isFavorite
                    )
                }
                .onFailure { error ->
                    _state.value = DetailsUiState(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить вакансию"
                    )
                }
        }
    }

    fun retry() {
        loadVacancy()
    }

    fun onBackPressed() {
        viewModelScope.launch {
            _events.emit(DetailsEvent.NavigateBack)
        }
    }

    fun onShareClicked() {
        val vacancy = _state.value.vacancy ?: return
        viewModelScope.launch {
            _events.emit(DetailsEvent.ShareVacancy(vacancy))
        }
    }

    fun onFavoriteClicked() {
        val vacancy = _state.value.vacancy ?: return

        viewModelScope.launch {
            val newFavoriteState = if (_state.value.isFavorite) {
                favoritesRepository.removeFromFavorites(vacancyId)
                false
            } else {
                favoritesRepository.addToFavorites(vacancy)
                true
            }

            _state.value = _state.value.copy(isFavorite = newFavoriteState)
        }
    }

    fun onPhoneClicked(phone: String) {
        viewModelScope.launch {
            _events.emit(DetailsEvent.DialPhone(phone))
        }
    }

    fun onEmailClicked(email: String) {
        viewModelScope.launch {
            _events.emit(DetailsEvent.SendEmail(email))
        }
    }

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            val isFavorite = favoritesRepository.isFavorite(vacancyId)
            _state.value = _state.value.copy(isFavorite = isFavorite)
        }
    }
}

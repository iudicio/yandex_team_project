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
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractor
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.VacancyCard
import kotlin.text.isNotBlank

sealed class DetailsEvent {
    object NavigateBack : DetailsEvent()
    data class ShareVacancy(val vacancy: VacancyDetailResult) : DetailsEvent()
    data class DialPhone(val phone: String) : DetailsEvent()
    data class SendEmail(val email: String) : DetailsEvent()
}

class DetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val vacancyId: String,
    private val vacancyDetailsInteractor: VacancyDetailsInteractor,
    private val favoritesInteractor: FavoritesInteractor,
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

            vacancyDetailsInteractor.getDetails(vacancyId)
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
        val vacancy = _state.value.vacancy?.toCard() ?: return
        viewModelScope.launch {
            val newFavoriteState = if (_state.value.isFavorite) {
                favoritesInteractor.remove(vacancyId)
                false
            } else {
                favoritesInteractor.add(vacancy)
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

    private fun VacancyDetailResult.toCard(): VacancyCard {
        return VacancyCard(
            id = id,
            name = name,
            company = employer.name,
            city = address?.city?.takeIf(String::isNotBlank),
            salary = salary?.toDomain(),
            logo = employer.logo.takeIf(String::isNotBlank),
        )
    }

    private fun Salary.toDomain(): Salary = Salary(
        from = from,
        to = to,
        currency = currency?.takeIf(String::isNotBlank),
    )

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isFavorite(vacancyId)
            _state.value = _state.value.copy(isFavorite = isFavorite)
        }
    }
}

package ru.practicum.android.diploma.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor

class FavoritesViewModel(
    interactor: FavoritesInteractor,
) : ViewModel() {
    val state: StateFlow<FavoritesUiState> = interactor
        .observeFavorites()
        .map { vacancies ->
            if (vacancies.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Content(vacancies)
            }
        }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            emit(FavoritesUiState.DatabaseError)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FavoritesUiState.Loading,
        )
}

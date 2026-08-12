package ru.practicum.android.diploma.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.practicum.android.diploma.domain.favorites.FavoritesModel
import ru.practicum.android.diploma.domain.favorites.InMemoryFavoritesModel

class FavoritesViewModel(
    private val model: FavoritesModel = InMemoryFavoritesModel(),
) : ViewModel() {

    val state: StateFlow<FavoritesUiState> = model.observeFavorites()
        .map { favorites ->
            if (favorites.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Content(favorites)
            }
        }
        .catch { emit(FavoritesUiState.DatabaseError) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = FavoritesUiState.Loading,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

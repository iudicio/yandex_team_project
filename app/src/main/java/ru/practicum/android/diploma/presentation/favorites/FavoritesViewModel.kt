package ru.practicum.android.diploma.presentation.favorites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.practicum.android.diploma.domain.favorites.FavoritesModel
import ru.practicum.android.diploma.domain.favorites.InMemoryFavoritesModel

class FavoritesViewModel(
    private val model: FavoritesModel = InMemoryFavoritesModel(),
) : ViewModel() {

    private val _state = MutableStateFlow(
        FavoritesUiState(favoritesCount = model.getFavoriteIds().size),
    )
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()
}

package ru.practicum.android.diploma.presentation.favorites

data class FavoritesUiState(
    val favoritesCount: Int,
    val isEmpty: Boolean = favoritesCount == 0
)

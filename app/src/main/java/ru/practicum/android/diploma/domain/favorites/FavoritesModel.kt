package ru.practicum.android.diploma.domain.favorites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface FavoritesModel {
    fun observeFavorites(): Flow<List<FavoriteVacancy>>
}

class InMemoryFavoritesModel(
    private val favorites: List<FavoriteVacancy> = emptyList(),
) : FavoritesModel {

    override fun observeFavorites(): Flow<List<FavoriteVacancy>> = flowOf(favorites)
}

data class FavoriteVacancy(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val salary: String,
)

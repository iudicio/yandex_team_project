package ru.practicum.android.diploma.domain.details

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addToFavorites(vacancy: VacancyDetailResult)
    suspend fun removeFromFavorites(vacancyId: String)
    suspend fun isFavorite(vacancyId: String): Boolean
    fun getFavoriteVacancies(): Flow<List<VacancyDetailResult>>
}

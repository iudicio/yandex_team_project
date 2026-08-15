package ru.practicum.android.diploma.domain.favorites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.practicum.android.diploma.domain.search.VacancyCard

interface FavoriteVacancyRepository {
    fun observeFavorites(): Flow<List<FavoriteVacancy>>
    suspend fun getFavorite(id: String): FavoriteVacancy?
    suspend fun isFavorite(id: String): Boolean
    suspend fun add(vacancy: FavoriteVacancy)
    suspend fun remove(id: String)
}

typealias FavoriteVacancy = VacancyCard

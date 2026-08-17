package ru.practicum.android.diploma.domain.favorites

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.details.VacancyDetailResult

interface FavoritesInteractor {
    fun observeFavorites(): Flow<List<FavoriteVacancy>>
    suspend fun getFavorite(id: String): FavoriteVacancy?
    suspend fun isFavorite(id: String): Boolean
    suspend fun add(vacancy: VacancyDetailResult)
    suspend fun remove(id: String)
}

class FavoritesInteractorImpl(
    private val repository: FavoriteVacancyRepository,
) : FavoritesInteractor {

    override fun observeFavorites(): Flow<List<FavoriteVacancy>> = repository.observeFavorites()
    override suspend fun getFavorite(id: String): FavoriteVacancy? = repository.getFavorite(id)
    override suspend fun isFavorite(id: String): Boolean = repository.isFavorite(id)
    override suspend fun add(vacancy: VacancyDetailResult) = repository.add(vacancy)
    override suspend fun remove(id: String) = repository.remove(id)
}

package ru.practicum.android.diploma.domain.favorites

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.VacancyCard

interface FavoritesInteractor {
    fun observeFavorites(): Flow<List<VacancyCard>>

    fun observeIsFavorite(vacancyId: String): Flow<Boolean>

    suspend fun getFavorite(vacancyId: String): VacancyDetailResult?

    suspend fun add(vacancy: VacancyDetailResult)

    suspend fun remove(vacancyId: String)
}

class FavoritesInteractorImpl(
    private val repository: FavoriteVacancyRepository,
) : FavoritesInteractor {
    override fun observeFavorites(): Flow<List<VacancyCard>> = repository.observeFavorites()

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = repository.observeIsFavorite(vacancyId)

    override suspend fun getFavorite(vacancyId: String): VacancyDetailResult? =
        repository.getFavorite(vacancyId)

    override suspend fun add(vacancy: VacancyDetailResult) {
        repository.add(vacancy)
    }

    override suspend fun remove(vacancyId: String) {
        repository.remove(vacancyId)
    }
}

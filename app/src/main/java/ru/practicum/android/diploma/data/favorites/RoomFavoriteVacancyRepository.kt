package ru.practicum.android.diploma.data.favorites

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository

class RoomFavoriteVacancyRepository(
    private val dao: FavoriteVacancyDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FavoriteVacancyRepository {

    override fun observeFavorites(): Flow<List<FavoriteVacancy>> =
        dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun getFavorite(id: String): FavoriteVacancy? =
        withContext(dispatcher) { dao.getById(id)?.toDomain() }

    override suspend fun isFavorite(id: String): Boolean =
        withContext(dispatcher) { dao.getById(id) != null }

    override suspend fun add(vacancy: FavoriteVacancy) {
        withContext(dispatcher) { dao.insert(vacancy.toEntity()) }
    }

    override suspend fun remove(id: String) {
        withContext(dispatcher) { dao.deleteById(id) }
    }
}

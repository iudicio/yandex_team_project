package ru.practicum.android.diploma.data.favorites

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.details.toCard
import ru.practicum.android.diploma.data.details.toEntity
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository

class RoomFavoriteVacancyRepository(
    private val dao: FavoriteVacancyDao,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FavoriteVacancyRepository {

    override fun observeFavorites(): Flow<List<FavoriteVacancy>> =
        dao.getAll()
            .map { entities -> entities.map { it.toCard(gson) } }
            .flowOn(dispatcher)

    override suspend fun getFavorite(id: String): FavoriteVacancy? =
        withContext(dispatcher) { dao.getById(id)?.toCard(gson) }

    override suspend fun isFavorite(id: String): Boolean =
        withContext(dispatcher) { dao.getCountById(id) > 0 }

    override suspend fun add(vacancy: VacancyDetailResult) {
        withContext(dispatcher) { dao.insert(vacancy.toEntity(gson)) }
    }

    override suspend fun remove(id: String) {
        withContext(dispatcher) { dao.deleteById(id) }
    }
}

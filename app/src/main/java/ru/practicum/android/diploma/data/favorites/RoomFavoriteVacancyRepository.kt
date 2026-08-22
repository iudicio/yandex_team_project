package ru.practicum.android.diploma.data.favorites

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancyRepository
import ru.practicum.android.diploma.domain.favorites.FavoritesDatabaseException
import ru.practicum.android.diploma.domain.search.VacancyCard

class RoomFavoriteVacancyRepository(
    private val dao: FavoriteVacancyDao,
    private val dispatchers: DispatcherProvider,
) : FavoriteVacancyRepository {
    override fun observeFavorites(): Flow<List<VacancyCard>> = dao
        .observeAll()
        .map { entities -> entities.map { entity -> entity.toVacancyCard() } }
        .wrapDatabaseErrors()
        .flowOn(dispatchers.io)

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = dao
        .observeIsFavorite(vacancyId)
        .wrapDatabaseErrors()
        .flowOn(dispatchers.io)

    override suspend fun getFavorite(vacancyId: String): VacancyDetailResult? = databaseCall {
        dao.findById(vacancyId)?.toVacancyDetails()
    }

    override suspend fun add(vacancy: VacancyDetailResult) {
        databaseCall { dao.add(vacancy.toFavoriteEntity()) }
    }

    override suspend fun remove(vacancyId: String) {
        databaseCall { dao.deleteById(vacancyId) }
    }

    @Suppress("detekt.TooGenericExceptionCaught")
    private suspend fun <T> databaseCall(block: suspend () -> T): T = withContext(dispatchers.io) {
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: FavoritesDatabaseException) {
            throw exception
        } catch (exception: Exception) {
            throw FavoritesDatabaseException(exception)
        }
    }
}

private fun <T> Flow<T>.wrapDatabaseErrors(): Flow<T> = catch { throwable ->
    when (throwable) {
        is CancellationException -> throw throwable
        is FavoritesDatabaseException -> throw throwable
        is Exception -> throw FavoritesDatabaseException(throwable)
        else -> throw throwable
    }
}

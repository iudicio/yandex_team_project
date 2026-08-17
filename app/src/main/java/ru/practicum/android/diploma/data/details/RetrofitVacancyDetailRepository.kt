package ru.practicum.android.diploma.data.details

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailResult

class RetrofitVacancyDetailRepository(
    private val api: DiplomaApi,
    private val dao: FavoriteVacancyDao,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VacancyDetailRepository {

    override suspend fun getVacancy(vacancyId: String): Result<VacancyDetailResult> =
        withContext(dispatcher) {
            resultOf {
                api.getVacancy(vacancyId).toDomain()
            }
                .recoverCatching { throwable ->
                    if (throwable is CancellationException) throw throwable
                    dao.getById(vacancyId)?.toDetail(gson) ?: throw throwable
                }
        }

    @Suppress("detekt.TooGenericExceptionCaught")
    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}

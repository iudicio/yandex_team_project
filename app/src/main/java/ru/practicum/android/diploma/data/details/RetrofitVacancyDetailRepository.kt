package ru.practicum.android.diploma.data.details

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.details.dto.VacancyDetailsRequestDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailsError
import ru.practicum.android.diploma.domain.details.VacancyDetailsOutcome

private const val HTTP_NOT_FOUND = 404

class RetrofitVacancyDetailRepository(
    private val api: VacancyDetailsApi,
    private val dispatcherProvider: DispatcherProvider,
) : VacancyDetailRepository {

    @Suppress("detekt.TooGenericExceptionCaught")
    override suspend fun getVacancy(vacancyId: String): VacancyDetailsOutcome {
        val request = VacancyDetailsRequestDto(vacancyId.trim())
        return try {
            val vacancy = withContext(dispatcherProvider.io) {
                api.getVacancy(request.vacancyId).toDomain(fallbackId = request.vacancyId)
            }
            VacancyDetailsOutcome.Content(vacancy = vacancy)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            VacancyDetailsOutcome.Failure(error = exception.toDetailsError())
        }
    }
}

private fun Throwable.toDetailsError(): VacancyDetailsError = when {
    this is NoInternetException -> VacancyDetailsError.NoInternet
    this is HttpException && code() == HTTP_NOT_FOUND -> VacancyDetailsError.NotFound
    else -> VacancyDetailsError.Server
}

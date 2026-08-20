package ru.practicum.android.diploma.domain.details

import kotlinx.coroutines.CancellationException
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor

interface VacancyDetailsInteractor {
    suspend fun getDetails(vacancyId: String): VacancyDetailsOutcome
}

class VacancyDetailsInteractorImpl(
    private val repository: VacancyDetailRepository,
    private val favoritesInteractor: FavoritesInteractor,
) : VacancyDetailsInteractor {

    override suspend fun getDetails(vacancyId: String): VacancyDetailsOutcome =
        when (val outcome = repository.getVacancy(vacancyId)) {
            is VacancyDetailsOutcome.Content -> outcome
            is VacancyDetailsOutcome.Failure -> handleFailure(vacancyId, outcome.error)
        }

    private suspend fun handleFailure(
        vacancyId: String,
        error: VacancyDetailsError,
    ): VacancyDetailsOutcome = when (error) {
        VacancyDetailsError.NoInternet -> loadOfflineFavorite(vacancyId)
        VacancyDetailsError.NotFound -> removeDeletedFavorite(vacancyId)
        VacancyDetailsError.Server -> VacancyDetailsOutcome.Failure(VacancyDetailsError.Server)
    }

    private suspend fun loadOfflineFavorite(vacancyId: String): VacancyDetailsOutcome = try {
        favoritesInteractor.getFavorite(vacancyId)?.let { cachedVacancy ->
            VacancyDetailsOutcome.Content(
                vacancy = cachedVacancy,
                isOffline = true,
            )
        } ?: VacancyDetailsOutcome.Failure(VacancyDetailsError.NoInternet)
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        VacancyDetailsOutcome.Failure(VacancyDetailsError.Server)
    }

    private suspend fun removeDeletedFavorite(vacancyId: String): VacancyDetailsOutcome = try {
        favoritesInteractor.remove(vacancyId)
        VacancyDetailsOutcome.Failure(VacancyDetailsError.NotFound)
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        VacancyDetailsOutcome.Failure(VacancyDetailsError.Server)
    }
}

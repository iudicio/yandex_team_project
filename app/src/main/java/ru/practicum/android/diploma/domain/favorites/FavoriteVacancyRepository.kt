package ru.practicum.android.diploma.domain.favorites

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.VacancyCard

interface FavoriteVacancyRepository {
    fun observeFavorites(): Flow<List<VacancyCard>>

    fun observeIsFavorite(vacancyId: String): Flow<Boolean>

    suspend fun getFavorite(vacancyId: String): VacancyDetailResult?

    suspend fun add(vacancy: VacancyDetailResult)

    suspend fun remove(vacancyId: String)
}

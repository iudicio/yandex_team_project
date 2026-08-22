package ru.practicum.android.diploma.data.filter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.filter.dto.IndustryDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.IndustriesRepository
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.sortedByRussianName

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class RetrofitIndustriesRepository(
    private val api: IndustriesApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IndustriesRepository {
    override suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>> = withContext(ioDispatcher) {
        try {
            FilterCatalogOutcome.Success(
                api.getIndustries()
                    .flatMap { dto -> dto?.flatten().orEmpty() }
                    .distinctBy(Industry::id)
                    .sortedByRussianName(Industry::name),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: NoInternetException) {
            FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet)
        } catch (_: Exception) {
            FilterCatalogOutcome.Failure(FilterCatalogError.Generic)
        }
    }
}

private fun IndustryDto.flatten(): List<Industry> {
    val current = toDomain()?.let(::listOf).orEmpty()
    val children = industries.orEmpty().flatMap { child -> child?.flatten().orEmpty() }
    return current + children
}

private fun IndustryDto.toDomain(): Industry? {
    val validId = id?.trim()?.takeIf(String::isNotEmpty)
    val validName = name?.trim()?.takeIf(String::isNotEmpty)
    return if (validId != null && validName != null) Industry(validId, validName) else null
}

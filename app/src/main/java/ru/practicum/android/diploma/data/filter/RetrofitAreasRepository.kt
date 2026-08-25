package ru.practicum.android.diploma.data.filter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.practicum.android.diploma.data.filter.dto.AreaDto
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.AreasRepository
import ru.practicum.android.diploma.domain.filter.FilterCatalogError
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class RetrofitAreasRepository(
    private val api: AreasApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AreasRepository {
    override suspend fun loadAreas(): FilterCatalogOutcome<List<Area>> = withContext(ioDispatcher) {
        try {
            FilterCatalogOutcome.Success(
                api.getAreas().mapNotNull { dto -> dto?.toDomain(fallbackParentId = null) }
                    .distinctBy(Area::id),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: NoInternetException) {
            FilterCatalogOutcome.Failure(FilterCatalogError.NoInternet)
        } catch (exception: HttpException) {
            FilterCatalogOutcome.Failure(exception.toFilterCatalogError())
        } catch (_: Exception) {
            FilterCatalogOutcome.Failure(FilterCatalogError.Generic)
        }
    }
}

private fun HttpException.toFilterCatalogError(): FilterCatalogError =
    if (code() in SERVER_ERROR_CODES) FilterCatalogError.Server else FilterCatalogError.Generic

private fun AreaDto.toDomain(fallbackParentId: Int?): Area? {
    val validId = id
    val validName = name?.trim()?.takeIf(String::isNotEmpty)
    return if (validId == null || validName == null) {
        null
    } else {
        Area(
            id = validId,
            name = validName,
            parentId = parentId ?: fallbackParentId,
            areas = areas.orEmpty().mapNotNull { child -> child?.toDomain(fallbackParentId = validId) }
                .distinctBy(Area::id),
        )
    }
}

private const val SERVER_ERROR_CODE_MIN = 500
private const val SERVER_ERROR_CODE_MAX = 599
private val SERVER_ERROR_CODES = SERVER_ERROR_CODE_MIN..SERVER_ERROR_CODE_MAX

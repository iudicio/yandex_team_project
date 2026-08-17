package ru.practicum.android.diploma.data.filter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.CatalogRepository
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.RegionSelection

class RetrofitCatalogRepository(
    private val api: DiplomaApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CatalogRepository {

    @Volatile
    private var areasCache: List<Area>? = null

    @Volatile
    private var industriesCache: List<Industry>? = null

    private val areasMutex = Mutex()
    private val industriesMutex = Mutex()

    override suspend fun getCountries(): Result<List<AreaSelection>> = withContext(dispatcher) {
        resultOf {
            getRootCountries().map { area ->
                AreaSelection(
                    id = area.id,
                    name = area.name,
                )
            }
        }
    }

    override suspend fun getRegions(countryId: Int?): Result<List<RegionSelection>> = withContext(dispatcher) {
        resultOf {
            val countries = getRootCountries()
            val requestedCountries = if (countryId == null) {
                countries
            } else {
                countries.filter { country -> country.id == countryId }
            }

            requestedCountries.flatMap { country ->
                country.areas.flatMap { area -> area.toRegionSelections(country) }
            }
                .distinctBy(RegionSelection::id)
                .sortedBy(RegionSelection::name)
        }
    }

    override suspend fun getIndustries(): Result<List<Industry>> = withContext(dispatcher) {
        resultOf {
            industriesCache ?: loadIndustries()
        }
    }

    private suspend fun getRootCountries(): List<Area> =
        getAreaTree().filter { area -> area.parentId == null }

    private suspend fun getAreaTree(): List<Area> = areasCache ?: loadAreas()

    private suspend fun loadAreas(): List<Area> {
        areasMutex.lock()
        return try {
            areasCache ?: api.getAreas()
                .mapNotNull { areaDto -> areaDto.toDomain(inheritedParentId = null) }
                .also { areas -> areasCache = areas }
        } finally {
            areasMutex.unlock()
        }
    }

    private suspend fun loadIndustries(): List<Industry> {
        industriesMutex.lock()
        return try {
            industriesCache ?: api.getIndustries()
                .mapNotNull(IndustryDto::toDomain)
                .also { industries -> industriesCache = industries }
        } finally {
            industriesMutex.unlock()
        }
    }
}

private fun AreaDto.toDomain(inheritedParentId: Int?): Area? {
    val validId = id
    val validName = name?.takeIf(String::isNotBlank)
    return if (validId == null || validName == null) {
        null
    } else {
        Area(
            id = validId,
            name = validName,
            parentId = parentId ?: inheritedParentId,
            areas = areas.orEmpty().mapNotNull { child ->
                child.toDomain(inheritedParentId = validId)
            },
        )
    }
}

private fun IndustryDto.toDomain(): Industry? {
    val validId = id
    val validName = name?.takeIf(String::isNotBlank)
    return if (validId == null || validName == null) {
        null
    } else {
        Industry(
            id = validId.toString(),
            name = validName,
        )
    }
}

private fun Area.toRegionSelections(rootCountry: Area): List<RegionSelection> = buildList {
    add(
        RegionSelection(
            id = id,
            name = name,
            countryId = rootCountry.id,
            countryName = rootCountry.name,
        ),
    )
    areas.forEach { child -> addAll(child.toRegionSelections(rootCountry)) }
}

@Suppress("detekt.TooGenericExceptionCaught")
private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}

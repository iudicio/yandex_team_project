package ru.practicum.android.diploma.data.filter

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.data.details.RetrofitVacancyDetailRepository
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.EmployerDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.data.network.dto.SalaryDto
import ru.practicum.android.diploma.data.network.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.network.dto.VacancyResponseDto
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.RegionSelection
import java.io.IOException

class RetrofitCatalogRepositoryTest {

    @Test
    fun `countries contain valid roots and areas are loaded once`() = runBlocking {
        val api = FakeDiplomaApi(areas = AREA_TREE + TOP_LEVEL_ORPHAN)
        val repository = RetrofitCatalogRepository(api)

        val firstResult = repository.getCountries()
        val secondResult = repository.getCountries()

        assertEquals(
            listOf(
                AreaSelection(id = RUSSIA_ID, name = RUSSIA),
                AreaSelection(id = BELARUS_ID, name = BELARUS),
            ),
            firstResult.getOrThrow(),
        )
        assertEquals(firstResult, secondResult)
        assertEquals(1, api.areaRequests)
    }

    @Test
    fun `all descendants become sorted regions associated with root country`() = runBlocking {
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areas = AREA_TREE + TOP_LEVEL_ORPHAN))

        val regions = repository.getRegions(countryId = null).getOrThrow()

        assertEquals(
            listOf(
                RegionSelection(id = MINSK_ID, name = MINSK, countryId = BELARUS_ID, countryName = BELARUS),
                RegionSelection(id = MOSCOW_ID, name = MOSCOW, countryId = RUSSIA_ID, countryName = RUSSIA),
                RegionSelection(id = TULA_ID, name = TULA, countryId = RUSSIA_ID, countryName = RUSSIA),
                RegionSelection(
                    id = CENTRAL_DISTRICT_ID,
                    name = CENTRAL_DISTRICT,
                    countryId = RUSSIA_ID,
                    countryName = RUSSIA,
                ),
            ),
            regions,
        )
    }

    @Test
    fun `country id limits regions to all descendants of that country`() = runBlocking {
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areas = AREA_TREE))

        val regions = repository.getRegions(countryId = RUSSIA_ID).getOrThrow()

        assertEquals(listOf(MOSCOW_ID, TULA_ID, CENTRAL_DISTRICT_ID), regions.map(RegionSelection::id))
        assertTrue(regions.all { region -> region.countryId == RUSSIA_ID })
        assertTrue(repository.getRegions(countryId = UNKNOWN_COUNTRY_ID).getOrThrow().isEmpty())
    }

    @Test
    fun `duplicate region ids are returned once`() = runBlocking {
        val duplicatedTree = AREA_TREE.map { country ->
            if (country.id == RUSSIA_ID) {
                country.copy(
                    areas = country.areas.orEmpty() + AreaDto(
                        id = MOSCOW_ID,
                        name = "Дубликат Москвы",
                        parentId = RUSSIA_ID,
                        areas = emptyList(),
                    ),
                )
            } else {
                country
            }
        }
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areas = duplicatedTree))

        val regions = repository.getRegions(countryId = RUSSIA_ID).getOrThrow()

        assertEquals(1, regions.count { region -> region.id == MOSCOW_ID })
        assertEquals(MOSCOW, regions.single { region -> region.id == MOSCOW_ID }.name)
    }

    @Test
    fun `industries skip malformed items convert ids and use cache`() = runBlocking {
        val api = FakeDiplomaApi(
            industries = listOf(
                IndustryDto(id = 7, name = "IT"),
                IndustryDto(id = null, name = "Без id"),
                IndustryDto(id = 8, name = ""),
                IndustryDto(id = 9, name = "Строительство"),
            ),
        )
        val repository = RetrofitCatalogRepository(api)

        val firstResult = repository.getIndustries()
        val secondResult = repository.getIndustries()

        assertEquals(
            listOf(
                Industry(id = "7", name = "IT"),
                Industry(id = "9", name = "Строительство"),
            ),
            firstResult.getOrThrow(),
        )
        assertEquals(firstResult, secondResult)
        assertEquals(1, api.industryRequests)
    }

    @Test
    fun `catalog failure keeps original throwable`() = runBlocking {
        val expected = IllegalStateException("catalog unavailable")
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areaFailure = expected))

        val result = repository.getCountries()

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    @Test
    fun `empty catalog response is a successful empty result`() = runBlocking {
        val repository = RetrofitCatalogRepository(FakeDiplomaApi())

        assertTrue(repository.getCountries().getOrThrow().isEmpty())
        assertTrue(repository.getRegions(countryId = null).getOrThrow().isEmpty())
        assertTrue(repository.getIndustries().getOrThrow().isEmpty())
    }

    @Test
    fun `failed areas request is not cached and can be retried`() = runBlocking {
        val expected = IllegalStateException("temporary failure")
        var calls = 0
        val api = object : DiplomaApi {
            override suspend fun getAreas(): List<AreaDto> {
                calls += 1
                if (calls == 1) throw expected
                return AREA_TREE
            }

            override suspend fun getIndustries(): List<IndustryDto> = emptyList()

            override suspend fun searchVacancies(
                text: String,
                area: Int?,
                industry: Int?,
                salary: Int?,
                page: Int?,
                onlyWithSalary: Boolean?,
            ): VacancyResponseDto = error("Search is not used by catalog tests")

            override suspend fun getVacancy(id: String): VacancyDetailResponseDto =
                error("Vacancy detail is not used by this test")
        }
        val repository = RetrofitCatalogRepository(api)

        assertSame(expected, repository.getCountries().exceptionOrNull())
        assertEquals(2, repository.getCountries().getOrThrow().size)
        assertEquals(2, calls)
    }

    @Test
    fun `vacancy detail returns expected data`() = runBlocking {
        val api = FakeDiplomaApi(
            vacancyDetail = VacancyDetailResponseDto(
                id = "123",
                name = "Разработчик Kotlin",
                description = "Some desc",
                salary = SalaryDto(1, 2, "Р"),
                address = null,
                experience = null,
                schedule = null,
                employment = null,
                contacts = null,
                employer = EmployerDto(id = "e1", name = "Company", logo = ""),
                area = AreaDto(id = 1, name = "Москва", parentId = null, areas = null),
                skills = emptyList(),
                url = "https://vacancy/123",
                industry = IndustryDto(id = 7, name = "IT"),
            ),
        )
        val repository = RetrofitVacancyDetailRepository(
            api,
            FakeFavoriteVacancyDao(),
            Gson(),
            Dispatchers.Unconfined,
        )

        val result = repository.getVacancy("123").getOrThrow()

        assertEquals("123", result.id)
        assertEquals("Разработчик Kotlin", result.name)
        assertEquals("Company", result.employer.name)
        assertEquals("Москва", result.area?.name)
        assertEquals("IT", result.industry?.name)
        assertEquals(1, api.vacancyRequests)
    }

    @Test
    fun `vacancy detail failure without cache is result failure`() = runBlocking {
        val expected = IOException("Network error")
        val repository = RetrofitVacancyDetailRepository(
            FakeDiplomaApi(vacancyFailure = expected),
            FakeFavoriteVacancyDao(),
            Gson(),
            Dispatchers.Unconfined,
        )

        val result = repository.getVacancy("123")

        assertTrue(result.isFailure)
        assertEquals(expected, result.exceptionOrNull())
    }

    @Test
    fun `vacancy detail falls back to cached favorite when offline`() = runBlocking {
        val dao = FakeFavoriteVacancyDao()
        dao.insert(
            FavoriteVacancyEntity(
                id = "123",
                name = "Cached vacancy",
                description = "desc",
                salaryJson = null,
                addressJson = null,
                experienceId = null,
                experienceName = null,
                scheduleId = null,
                scheduleName = null,
                employmentId = null,
                employmentName = null,
                contactsJson = null,
                employerJson = """{"id":"e1","name":"Cached Corp","logo":null}""",
                areaJson = null,
                skillsJson = "[]",
                url = "https://vacancy/123",
                industryId = null,
                industryName = null,
            ),
        )
        val repository = RetrofitVacancyDetailRepository(
            FakeDiplomaApi(vacancyFailure = IOException("offline")),
            dao,
            Gson(),
            Dispatchers.Unconfined,
        )

        val result = repository.getVacancy("123").getOrThrow()

        assertEquals("Cached vacancy", result.name)
        assertEquals("Cached Corp", result.employer.name)
    }

    @Test
    fun `catalog cancellation is rethrown`() {
        val expected = CancellationException("cancelled")
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areaFailure = expected))

        val actual = try {
            runBlocking { repository.getRegions(countryId = null) }
            null
        } catch (exception: CancellationException) {
            exception
        }

        assertEquals(expected.message, actual?.message)
    }

    @Test
    fun `catalog fatal errors are not converted to result failure`() {
        val expected = AssertionError("fatal")
        val repository = RetrofitCatalogRepository(FakeDiplomaApi(areaFailure = expected))

        val actual = try {
            runBlocking { repository.getCountries() }
            null
        } catch (error: AssertionError) {
            error
        }

        assertEquals(expected.message, actual?.message)
    }
}

private class FakeFavoriteVacancyDao : FavoriteVacancyDao {
    private val storage = linkedMapOf<String, FavoriteVacancyEntity>()

    override suspend fun insert(vacancy: FavoriteVacancyEntity) {
        storage[vacancy.id] = vacancy
    }

    override suspend fun getById(vacancyId: String): FavoriteVacancyEntity? = storage[vacancyId]

    override fun getAll(): Flow<List<FavoriteVacancyEntity>> = flowOf(storage.values.toList())

    override suspend fun deleteById(vacancyId: String) {
        storage.remove(vacancyId)
    }

    override suspend fun getCountById(vacancyId: String): Int = if (storage.containsKey(vacancyId)) 1 else 0
}

private class FakeDiplomaApi(
    private val areas: List<AreaDto> = emptyList(),
    private val industries: List<IndustryDto> = emptyList(),
    private val areaFailure: Throwable? = null,
    private val industryFailure: Throwable? = null,
    private val vacancyDetail: VacancyDetailResponseDto? = null,
    private val vacancyFailure: Throwable? = null,
) : DiplomaApi {
    var areaRequests = 0
    var industryRequests = 0
    var vacancyRequests = 0

    override suspend fun getAreas(): List<AreaDto> {
        areaRequests += 1
        areaFailure?.let { throwable -> throw throwable }
        return areas
    }

    override suspend fun getIndustries(): List<IndustryDto> {
        industryRequests += 1
        industryFailure?.let { throwable -> throw throwable }
        return industries
    }

    override suspend fun searchVacancies(
        text: String,
        area: Int?,
        industry: Int?,
        salary: Int?,
        page: Int?,
        onlyWithSalary: Boolean?,
    ): VacancyResponseDto = error("Search is not used by catalog tests")

    override suspend fun getVacancy(id: String): VacancyDetailResponseDto {
        vacancyRequests += 1
        vacancyFailure?.let { throwable -> throw throwable }
        return vacancyDetail ?: error("Vacancy detail not provided for id: $id")
    }
}

private const val RUSSIA_ID = 1
private const val BELARUS_ID = 2
private const val MOSCOW_ID = 10
private const val CENTRAL_DISTRICT_ID = 11
private const val TULA_ID = 111
private const val MINSK_ID = 20
private const val UNKNOWN_COUNTRY_ID = 404
private const val RUSSIA = "Россия"
private const val BELARUS = "Беларусь"
private const val MOSCOW = "Москва"
private const val CENTRAL_DISTRICT = "Центральный округ"
private const val TULA = "Тула"
private const val MINSK = "Минск"

private val AREA_TREE = listOf(
    AreaDto(
        id = RUSSIA_ID,
        name = RUSSIA,
        parentId = null,
        areas = listOf(
            AreaDto(id = MOSCOW_ID, name = MOSCOW, parentId = RUSSIA_ID, areas = emptyList()),
            AreaDto(
                id = CENTRAL_DISTRICT_ID,
                name = CENTRAL_DISTRICT,
                parentId = null,
                areas = listOf(
                    AreaDto(id = TULA_ID, name = TULA, parentId = null, areas = emptyList()),
                    AreaDto(id = null, name = "Некорректный", parentId = null, areas = emptyList()),
                ),
            ),
        ),
    ),
    AreaDto(
        id = BELARUS_ID,
        name = BELARUS,
        parentId = null,
        areas = listOf(AreaDto(id = MINSK_ID, name = MINSK, parentId = BELARUS_ID, areas = emptyList())),
    ),
    AreaDto(id = null, name = "Некорректная страна", parentId = null, areas = emptyList()),
    AreaDto(id = 3, name = " ", parentId = null, areas = emptyList()),
)

private val TOP_LEVEL_ORPHAN = AreaDto(
    id = 30,
    name = "Не корневая страна",
    parentId = RUSSIA_ID,
    areas = listOf(AreaDto(id = 31, name = "Регион сироты", parentId = 30, areas = emptyList())),
)

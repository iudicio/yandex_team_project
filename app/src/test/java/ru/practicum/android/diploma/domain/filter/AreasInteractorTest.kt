package ru.practicum.android.diploma.domain.filter

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AreasInteractorTest {
    private val tree = listOf(
        Area(
            id = 113,
            name = "Россия",
            areas = listOf(
                Area(1, "Москва", 113, areas = listOf(Area(101, "ЦАО", 1))),
                Area(2, "Санкт-Петербург", 113),
            ),
        ),
        Area(40, "Казахстан", areas = listOf(Area(160, "Алматы", 40))),
        Area(999, "not a root", parentId = 113),
    )

    @Test
    fun `countries include roots only and regions respect selected country`() {
        val interactor = interactor()

        assertEquals(listOf(40, 113), interactor.countries(tree).map(Area::id))
        assertEquals(listOf(1, 2, 101), interactor.regions(tree, countryId = 113).map(Area::id))
        assertEquals(listOf(160, 1, 2, 101), interactor.regions(tree, countryId = null).map(Area::id))
        assertTrue(interactor.regions(tree, countryId = -1).isEmpty())
    }

    @Test
    fun `finds root country for deeply nested region`() {
        val interactor = interactor()

        assertEquals(113, interactor.countryForRegion(tree, 101)?.id)
        assertEquals(40, interactor.countryForRegion(tree, 160)?.id)
        assertNull(interactor.countryForRegion(tree, 404))
    }

    @Test
    fun `sorts names with russian collation including yo`() {
        val roots = listOf(
            Area(1, "Яма"),
            Area(2, "Ёж"),
            Area(3, "Жук"),
        )

        assertEquals(listOf(2, 3, 1), interactor().countries(roots).map(Area::id))
    }

    @Test
    fun `load delegates typed outcome`() = runBlocking {
        val expected = FilterCatalogOutcome.Success(tree)

        assertEquals(expected, interactor(expected).loadAreas())
    }

    @Test
    fun `filter settings derives ids and active state`() {
        val empty = FilterSettings()
        val countryOnly = FilterSettings(country = Area(113, "Россия"))
        val region = countryOnly.copy(region = Area(1, "Москва", 113))

        assertFalse(empty.hasActiveFilters)
        assertNull(empty.areaId)
        assertTrue(countryOnly.hasActiveFilters)
        assertEquals(113, countryOnly.areaId)
        assertEquals(1, region.areaId)
        assertEquals("7.540", FilterSettings(industry = Industry("7.540", "IT")).industryId)
    }

    private fun interactor(
        outcome: FilterCatalogOutcome<List<Area>> = FilterCatalogOutcome.Success(tree),
    ): AreasInteractor = AreasInteractorImpl(FakeAreasRepository(outcome))
}

private class FakeAreasRepository(
    private val outcome: FilterCatalogOutcome<List<Area>>,
) : AreasRepository {
    override suspend fun loadAreas(): FilterCatalogOutcome<List<Area>> = outcome
}

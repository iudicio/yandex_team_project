package ru.practicum.android.diploma.domain.filter

interface AreasInteractor {
    suspend fun loadAreas(): FilterCatalogOutcome<List<Area>>
    fun countries(areas: List<Area>): List<Area>
    fun regions(areas: List<Area>, countryId: Int?): List<Area>
    fun countryForRegion(areas: List<Area>, regionId: Int): Area?
}

class AreasInteractorImpl(
    private val repository: AreasRepository,
) : AreasInteractor {
    override suspend fun loadAreas(): FilterCatalogOutcome<List<Area>> = repository.loadAreas()

    override fun countries(areas: List<Area>): List<Area> = areas
        .filter { area -> area.parentId == null }
        .distinctBy(Area::id)
        .sortedByRussianName(Area::name)

    override fun regions(areas: List<Area>, countryId: Int?): List<Area> {
        val countries = countries(areas)
        val roots = countryId?.let { id -> countries.filter { country -> country.id == id } } ?: countries
        return roots.flatMap(Area::descendants)
            .distinctBy(Area::id)
            .sortedByRussianName(Area::name)
    }

    override fun countryForRegion(areas: List<Area>, regionId: Int): Area? = countries(areas)
        .firstOrNull { country -> country.containsDescendant(regionId) }
}

private fun Area.descendants(): List<Area> = areas.flatMap { child ->
    listOf(child) + child.descendants()
}

private fun Area.containsDescendant(id: Int): Boolean = areas.any { child ->
    child.id == id || child.containsDescendant(id)
}

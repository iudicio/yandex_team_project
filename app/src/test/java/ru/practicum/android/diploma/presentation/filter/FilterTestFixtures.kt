package ru.practicum.android.diploma.presentation.filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.AreasInteractor
import ru.practicum.android.diploma.domain.filter.AreasInteractorImpl
import ru.practicum.android.diploma.domain.filter.AreasRepository
import ru.practicum.android.diploma.domain.filter.FilterCatalogOutcome
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor

internal class FakeFilterSettingsInteractor(
    initial: FilterSettings = FilterSettings(),
) : FilterSettingsInteractor {
    private val settings = MutableStateFlow(initial)
    private var appliedSettings = initial
    val saved = mutableListOf<FilterSettings>()
    var resetCount = 0

    override fun observe(): StateFlow<FilterSettings> = settings

    override fun current(): FilterSettings = settings.value

    override fun applied(): FilterSettings = appliedSettings

    override fun save(settings: FilterSettings) {
        saved += settings
        this.settings.value = settings
    }

    override fun reset() {
        resetCount += 1
        settings.value = FilterSettings()
    }

    override fun markApplied(settings: FilterSettings) {
        appliedSettings = settings
    }
}

internal class QueueAreasRepository(
    outcomes: List<FilterCatalogOutcome<List<Area>>>,
) : AreasRepository {
    private val outcomes = ArrayDeque(outcomes)
    var calls = 0

    override suspend fun loadAreas(): FilterCatalogOutcome<List<Area>> {
        calls += 1
        return outcomes.removeFirst()
    }
}

internal fun areasInteractor(
    vararg outcomes: FilterCatalogOutcome<List<Area>>,
): Pair<AreasInteractor, QueueAreasRepository> {
    val repository = QueueAreasRepository(outcomes.toList())
    return AreasInteractorImpl(repository) to repository
}

internal fun areaTree(): List<Area> = listOf(
    Area(
        id = 113,
        name = "Россия",
        areas = listOf(
            Area(1, "Москва", 113, areas = listOf(Area(101, "ЦАО", 1))),
            Area(2, "Санкт-Петербург", 113),
        ),
    ),
    Area(40, "Казахстан", areas = listOf(Area(160, "Алматы", 40))),
)

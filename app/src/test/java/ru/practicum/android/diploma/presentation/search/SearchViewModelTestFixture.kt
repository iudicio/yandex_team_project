@file:Suppress("detekt.MatchingDeclarationName")

package ru.practicum.android.diploma.presentation.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchPage
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import java.util.ArrayDeque

internal class QueueVacancyRepository : VacancyRepository {
    val requests = mutableListOf<VacancySearchRequest>()
    private val outcomes = ArrayDeque<SearchOutcome<VacancySearchPage>>()

    fun enqueue(outcome: SearchOutcome<VacancySearchPage>) {
        outcomes.addLast(outcome)
    }

    override suspend fun search(request: VacancySearchRequest): SearchOutcome<VacancySearchPage> {
        requests += request
        return outcomes.removeFirst()
    }
}

internal class FakeFilterSettingsInteractor(
    initial: FilterSettings = FilterSettings(),
) : FilterSettingsInteractor {
    private val settings = MutableStateFlow(initial)
    private var appliedSettings = initial

    override fun observe(): StateFlow<FilterSettings> = settings

    override fun current(): FilterSettings = settings.value

    override fun applied(): FilterSettings = appliedSettings

    override fun save(settings: FilterSettings) {
        this.settings.value = settings
    }

    override fun reset() {
        settings.value = FilterSettings()
    }

    override fun markApplied(settings: FilterSettings) {
        appliedSettings = settings
    }
}

internal fun page(
    page: Int = 1,
    pages: Int = 1,
    found: Int = 1,
    items: List<VacancyCard> = listOf(vacancy("1")),
): VacancySearchPage = VacancySearchPage(found = found, pages = pages, page = page, items = items)

internal fun vacancy(id: String): VacancyCard = VacancyCard(id = id, name = "Vacancy $id")

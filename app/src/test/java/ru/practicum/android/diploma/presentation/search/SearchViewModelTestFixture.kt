@file:Suppress("detekt.MatchingDeclarationName")

package ru.practicum.android.diploma.presentation.search

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

internal fun page(
    page: Int = 1,
    pages: Int = 1,
    found: Int = 1,
    items: List<VacancyCard> = listOf(vacancy("1")),
): VacancySearchPage = VacancySearchPage(found = found, pages = pages, page = page, items = items)

internal fun vacancy(id: String): VacancyCard = VacancyCard(id = id, name = "Vacancy $id")

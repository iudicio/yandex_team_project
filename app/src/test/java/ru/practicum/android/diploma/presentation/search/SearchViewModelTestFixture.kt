package ru.practicum.android.diploma.presentation.search

import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import ru.practicum.android.diploma.domain.search.VacancySearchResult
import java.util.ArrayDeque
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SearchViewModelTestFixture {

    class MutableFilterRepository(
        var settings: FilterSettings = FilterSettings(),
    ) : FilterSettingsRepository {
        override fun load(): FilterSettings = settings

        override fun save(settings: FilterSettings) {
            this.settings = settings
        }
    }

    class QueueVacancyRepository : VacancyRepository {
        val requests = mutableListOf<VacancySearchRequest>()
        private val results = ArrayDeque<Result<VacancySearchResult>>()

        fun enqueue(result: Result<VacancySearchResult>) {
            results.addLast(result)
        }

        override suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult> {
            requests += request
            return results.removeFirst()
        }
    }

    class ManualVacancyRepository : VacancyRepository {
        val requests = mutableListOf<VacancySearchRequest>()
        private val pending = mutableListOf<PendingSearch>()

        override suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult> =
            suspendCoroutine { continuation ->
                requests += request
                pending += PendingSearch(request, continuation)
            }

        fun complete(
            query: String,
            page: Int,
            result: Result<VacancySearchResult>,
        ) {
            val index = pending.indexOfFirst { search ->
                search.request.text == query && search.request.page == page
            }
            check(index >= 0) { "No pending request for query=$query page=$page" }
            pending.removeAt(index).continuation.resume(result)
        }
    }

    fun vacancy(id: String): VacancyCard = VacancyCard(
        id = id,
        name = "Vacancy $id",
    )

    fun result(
        page: Int = 1,
        pages: Int = 1,
        found: Int = 1,
        items: List<VacancyCard> = listOf(vacancy("1")),
    ): VacancySearchResult = VacancySearchResult(
        found = found,
        pages = pages,
        page = page,
        items = items,
    )

    private data class PendingSearch(
        val request: VacancySearchRequest,
        val continuation: Continuation<Result<VacancySearchResult>>,
    )
}

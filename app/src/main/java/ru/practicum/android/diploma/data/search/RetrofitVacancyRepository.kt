package ru.practicum.android.diploma.data.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.dto.VacancyCardDto
import ru.practicum.android.diploma.data.network.dto.VacancyCardSalaryDto
import ru.practicum.android.diploma.data.network.dto.VacancyResponseDto
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import ru.practicum.android.diploma.domain.search.VacancySearchResult

class RetrofitVacancyRepository(
    private val api: DiplomaApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VacancyRepository {

    override suspend fun search(request: VacancySearchRequest): Result<VacancySearchResult> = withContext(dispatcher) {
        resultOf {
            api.searchVacancies(
                text = request.text,
                area = request.area,
                industry = request.industry,
                salary = request.salary,
                page = request.page,
                onlyWithSalary = request.onlyWithSalary,
            ).toDomain(fallbackPage = request.page)
        }
    }
}

private fun VacancyResponseDto.toDomain(fallbackPage: Int): VacancySearchResult = VacancySearchResult(
    found = found?.coerceAtLeast(0) ?: 0,
    pages = pages?.coerceAtLeast(0) ?: 0,
    page = page ?: fallbackPage,
    items = items.orEmpty().mapNotNull { vacancy -> vacancy?.toDomain() },
)

private fun VacancyCardDto.toDomain(): VacancyCard? {
    val validId = id?.takeIf(String::isNotBlank)
    val validName = name?.takeIf(String::isNotBlank)
    return if (validId == null || validName == null) {
        null
    } else {
        VacancyCard(
            id = validId,
            name = validName,
            company = company?.takeIf(String::isNotBlank),
            city = city?.takeIf(String::isNotBlank),
            salary = salary?.toDomain(),
            logo = logo?.takeIf(String::isNotBlank),
        )
    }
}

private fun VacancyCardSalaryDto.toDomain(): Salary = Salary(
    from = from,
    to = to,
    currency = currency?.takeIf(String::isNotBlank),
)

@Suppress("detekt.TooGenericExceptionCaught")
private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}

package ru.practicum.android.diploma.data.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.data.search.dto.VacancyCardDto
import ru.practicum.android.diploma.data.search.dto.VacancySalaryDto
import ru.practicum.android.diploma.data.search.dto.VacancySearchRequestDto
import ru.practicum.android.diploma.data.search.dto.VacancySearchResponseDto
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchPage
import ru.practicum.android.diploma.domain.search.VacancySearchRequest

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class RetrofitVacancyRepository(
    private val api: VacancySearchApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VacancyRepository {

    override suspend fun search(request: VacancySearchRequest): SearchOutcome<VacancySearchPage> =
        withContext(ioDispatcher) {
            val requestDto = request.toDto()
            try {
                SearchOutcome.Success(
                    api.searchVacancies(
                        text = requestDto.text,
                        page = requestDto.page,
                        salary = requestDto.salary,
                        onlyWithSalary = requestDto.onlyWithSalary,
                        industryId = requestDto.industryId,
                        areaId = requestDto.areaId,
                    )
                        .toDomain(fallbackPage = requestDto.page),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: NoInternetException) {
                SearchOutcome.Failure(SearchError.NoInternet)
            } catch (_: Exception) {
                SearchOutcome.Failure(SearchError.Generic)
            }
        }
}

private fun VacancySearchRequest.toDto(): VacancySearchRequestDto = VacancySearchRequestDto(
    text = text.trim(),
    page = page,
    salary = salary,
    onlyWithSalary = onlyWithSalary.takeIf { it },
    industryId = industryId,
    areaId = areaId,
)

private fun VacancySearchResponseDto.toDomain(fallbackPage: Int): VacancySearchPage = VacancySearchPage(
    found = found?.coerceAtLeast(0) ?: 0,
    pages = pages?.coerceAtLeast(0) ?: 0,
    page = page?.coerceAtLeast(VacancySearchRequest.FIRST_PAGE) ?: fallbackPage,
    items = items.orEmpty().mapNotNull { item -> item?.toDomain() },
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

private fun VacancySalaryDto.toDomain(): Salary = Salary(
    from = from,
    to = to,
    currency = currency?.takeIf(String::isNotBlank),
)

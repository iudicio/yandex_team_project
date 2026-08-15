package ru.practicum.android.diploma.data.details

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.network.DiplomaApi
import ru.practicum.android.diploma.data.network.dto.AddressDto
import ru.practicum.android.diploma.data.network.dto.AreaDto
import ru.practicum.android.diploma.data.network.dto.BaseDetailDataDto
import ru.practicum.android.diploma.data.network.dto.ContactsDto
import ru.practicum.android.diploma.data.network.dto.EmployerDto
import ru.practicum.android.diploma.data.network.dto.IndustryDto
import ru.practicum.android.diploma.data.network.dto.PhoneDto
import ru.practicum.android.diploma.data.network.dto.SalaryDto
import ru.practicum.android.diploma.data.network.dto.VacancyDetailResponseDto
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.Salary

class RetrofitVacancyDetailRepository(
    private val api: DiplomaApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VacancyDetailRepository {

    override suspend fun getVacancy(vacancyId: String): Result<VacancyDetailResult> =
        withContext(dispatcher) {
            resultOf {
                api.getVacancy(vacancyId).toDomain()
            }
        }

    @Suppress("detekt.TooGenericExceptionCaught")
    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    private fun VacancyDetailResponseDto.toDomain(): VacancyDetailResult {
        return VacancyDetailResult(
            id = id,
            name = name,
            description = description,
            salary = salary?.toDomain(),
            address = address?.toDomain(),
            experience = experience?.toDomain(),
            schedule = schedule?.toDomain(),
            employment = employment?.toDomain(),
            contacts = contacts?.toDomain(),
            employer = employer.toDomain(),
            area = area.toDomain(null),
            skills = skills.orEmpty(),
            url = url,
            industry = industry.toDomain(),
        )
    }

    private fun SalaryDto.toDomain(): Salary = Salary(
        from = from,
        to = to,
        currency = currency?.takeIf(String::isNotBlank),
    )

    private fun AddressDto.toDomain(): Address = Address(
        id = id,
        city = city,
        street = street,
        building = building,
        raw = raw
    )

    private fun BaseDetailDataDto.toDomain(): BaseDetailData = BaseDetailData(
        id = id,
        name = name
    )

    private fun ContactsDto.toDomain(): Contacts = Contacts(
        id = id,
        name = name,
        email = email,
        phones = phones.map { it.toDomain() }
    )

    private fun PhoneDto.toDomain(): Phone = Phone(
        comment = comment,
        formatted = formatted
    )

    private fun EmployerDto.toDomain(): Employer = Employer(
        id = id,
        name = name,
        logo = logo
    )

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
}

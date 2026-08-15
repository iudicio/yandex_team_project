package ru.practicum.android.diploma.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.search.Salary

@Entity(tableName = "favorite_vacancies")
data class FavoriteVacancyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val company: String?,
    val city: String?,
    val salaryFrom: Int?,
    val salaryTo: Int?,
    val salaryCurrency: String?,
    val logo: String?,
    val addedAt: Long,
)

internal fun FavoriteVacancyEntity.toDomain(): FavoriteVacancy = FavoriteVacancy(
    id = id,
    name = name,
    company = company,
    city = city,
    salary = if (salaryFrom != null || salaryTo != null || salaryCurrency != null) {
        Salary(from = salaryFrom, to = salaryTo, currency = salaryCurrency)
    } else {
        null
    },
    logo = logo,
)

internal fun FavoriteVacancy.toEntity(addedAt: Long = System.currentTimeMillis()): FavoriteVacancyEntity =
    FavoriteVacancyEntity(
        id = id,
        name = name,
        company = company,
        city = city,
        salaryFrom = salary?.from,
        salaryTo = salary?.to,
        salaryCurrency = salary?.currency,
        logo = logo,
        addedAt = addedAt,
    )

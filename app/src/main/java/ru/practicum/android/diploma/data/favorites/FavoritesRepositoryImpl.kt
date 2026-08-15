package ru.practicum.android.diploma.data.favorites

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.FavoritesRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.Salary

class FavoritesRepositoryImpl(
    private val dao: FavoriteVacancyDao,
    private val gson: Gson = Gson(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoritesRepository {

    override suspend fun addToFavorites(vacancy: VacancyDetailResult) {
        withContext(dispatcher) {
            dao.insert(vacancy.toEntity())
        }
    }

    override suspend fun removeFromFavorites(vacancyId: String) {
        withContext(dispatcher) {
            dao.deleteById(vacancyId)
        }
    }

    override suspend fun isFavorite(vacancyId: String): Boolean {
        return withContext(dispatcher) {
            dao.getById(vacancyId) != null
        }
    }

    override fun getFavoriteVacancies(): Flow<List<VacancyDetailResult>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Мапперы
    private fun VacancyDetailResult.toEntity(): FavoriteVacancyEntity {
        return FavoriteVacancyEntity(
            id = id,
            name = name,
            description = description,
            salaryJson = salary?.let { gson.toJson(it) },
            addressJson = address?.let { gson.toJson(it) },
            experienceId = experience?.id,
            experienceName = experience?.name,
            scheduleId = schedule?.id,
            scheduleName = schedule?.name,
            employmentId = employment?.id,
            employmentName = employment?.name,
            contactsJson = contacts?.let { gson.toJson(it) },
            employerJson = gson.toJson(employer),
            areaJson = area?.let { gson.toJson(it) },
            skillsJson = gson.toJson(skills),
            url = url,
            industryId = industry?.id,
            industryName = industry?.name
        )
    }

    private fun FavoriteVacancyEntity.toDomain(): VacancyDetailResult {
        val salaryType = object : TypeToken<Salary>() {}.type
        val addressType = object : TypeToken<Address>() {}.type
        val contactsType = object : TypeToken<Contacts>() {}.type
        val employerType = object : TypeToken<Employer>() {}.type
        val areaType = object : TypeToken<Area>() {}.type
        val skillsType = object : TypeToken<List<String>>() {}.type

        return VacancyDetailResult(
            id = id,
            name = name,
            description = description,
            salary = salaryJson?.let { gson.fromJson(it, salaryType) },
            address = addressJson?.let { gson.fromJson(it, addressType) },
            experience = experienceId?.let {
                BaseDetailData(id = experienceId, name = experienceName ?: "")
            },
            schedule = scheduleId?.let {
                BaseDetailData(id = scheduleId, name = scheduleName ?: "")
            },
            employment = employmentId?.let {
                BaseDetailData(id = employmentId, name = employmentName ?: "")
            },
            contacts = contactsJson?.let { gson.fromJson(it, contactsType) },
            employer = gson.fromJson(employerJson, employerType),
            area = areaJson?.let { gson.fromJson(it, areaType) },
            skills = gson.fromJson(skillsJson, skillsType),
            url = url,
            industry = industryId?.let {
                Industry(id = industryId, name = industryName ?: "")
            }
        )
    }
}

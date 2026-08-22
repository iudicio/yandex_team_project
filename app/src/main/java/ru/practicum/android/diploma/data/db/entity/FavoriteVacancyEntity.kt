package ru.practicum.android.diploma.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_vacancies")
data class FavoriteVacancyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val salaryFrom: Int?,
    val salaryTo: Int?,
    val salaryCurrency: String?,
    val addressId: String?,
    val addressCity: String?,
    val addressStreet: String?,
    val addressBuilding: String?,
    val addressRaw: String?,
    val experienceId: String?,
    val experienceName: String?,
    val scheduleId: String?,
    val scheduleName: String?,
    val employmentId: String?,
    val employmentName: String?,
    val contactsId: String?,
    val contactsName: String?,
    val contactsEmail: String?,
    val contactsPhones: List<FavoritePhoneSnapshot>,
    val employerId: String?,
    val employerName: String?,
    val employerLogo: String?,
    val areaId: Int?,
    val areaName: String?,
    val skills: List<String>,
    val url: String,
    val industryId: Int?,
    val industryName: String?,
    val addedAt: Long,
    val legacyPayload: String? = null,
)

data class FavoritePhoneSnapshot(
    val comment: String?,
    val formatted: String?,
)

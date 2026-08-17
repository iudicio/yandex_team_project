package ru.practicum.android.diploma.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_vacancies")
data class FavoriteVacancyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val salaryJson: String?,
    val addressJson: String?,
    val experienceId: String?,
    val experienceName: String?,
    val scheduleId: String?,
    val scheduleName: String?,
    val employmentId: String?,
    val employmentName: String?,
    val contactsJson: String?,
    val employerJson: String,
    val areaJson: String?,
    val skillsJson: String,
    val url: String,
    val industryId: String?,
    val industryName: String?,
    val addedToFavoritesAt: Long = System.currentTimeMillis()
)

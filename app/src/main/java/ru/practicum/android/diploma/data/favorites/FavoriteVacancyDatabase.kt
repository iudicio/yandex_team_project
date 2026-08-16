package ru.practicum.android.diploma.data.favorites

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteVacancyEntity::class], version = 1, exportSchema = false)
abstract class FavoriteVacancyDatabase : RoomDatabase() {
    abstract fun favoriteVacancyDao(): FavoriteVacancyDao

    companion object {
        const val DATABASE_NAME = "favorite_vacancies.db"
    }
}

package ru.practicum.android.diploma.data.favorites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteVacancyDao {

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteVacancyEntity>>

    @Query("SELECT * FROM favorite_vacancies WHERE id = :id")
    suspend fun getById(id: String): FavoriteVacancyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vacancy: FavoriteVacancyEntity)

    @Query("DELETE FROM favorite_vacancies WHERE id = :id")
    suspend fun deleteById(id: String)
}

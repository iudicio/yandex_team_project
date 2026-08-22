package ru.practicum.android.diploma.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity

@Dao
interface FavoriteVacancyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vacancy: FavoriteVacancyEntity)

    @Query("DELETE FROM favorite_vacancies WHERE id = :vacancyId")
    suspend fun deleteById(vacancyId: String)

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteVacancyEntity>>

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavoriteVacancyEntity>

    @Query("SELECT * FROM favorite_vacancies WHERE id = :vacancyId LIMIT 1")
    suspend fun findById(vacancyId: String): FavoriteVacancyEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_vacancies WHERE id = :vacancyId)")
    fun observeIsFavorite(vacancyId: String): Flow<Boolean>
}

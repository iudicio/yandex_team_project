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
    suspend fun upsert(vacancy: FavoriteVacancyEntity)

    @Query("SELECT * FROM favorite_vacancies WHERE vacancyId = :vacancyId")
    suspend fun findById(vacancyId: String): FavoriteVacancyEntity?

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteVacancyEntity>>

    @Query("DELETE FROM favorite_vacancies WHERE vacancyId = :vacancyId")
    suspend fun deleteById(vacancyId: String)
}

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
    suspend fun insert(vacancy: FavoriteVacancyEntity)

    @Query("SELECT * FROM favorite_vacancies WHERE id = :vacancyId")
    suspend fun getById(vacancyId: String): FavoriteVacancyEntity?

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedToFavoritesAt DESC")
    fun getAll(): Flow<List<FavoriteVacancyEntity>>

    @Query("DELETE FROM favorite_vacancies WHERE id = :vacancyId")
    suspend fun deleteById(vacancyId: String)

    @Query("SELECT COUNT(*) FROM favorite_vacancies WHERE id = :vacancyId")
    suspend fun getCountById(vacancyId: String): Int
}

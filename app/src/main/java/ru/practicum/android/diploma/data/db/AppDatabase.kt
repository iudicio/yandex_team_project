package ru.practicum.android.diploma.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.data.favorites.FavoriteSnapshotConverters

@Database(
    entities = [FavoriteVacancyEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(FavoriteSnapshotConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteVacancyDao(): FavoriteVacancyDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorite_vacancies RENAME TO favorite_vacancies_legacy")
                db.execSQL(CREATE_FAVORITES_TABLE)
                db.execSQL(MIGRATE_LEGACY_FAVORITES)
                db.execSQL("DROP TABLE favorite_vacancies_legacy")
            }
        }

        private val CREATE_FAVORITES_TABLE = """
            CREATE TABLE IF NOT EXISTS favorite_vacancies (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                salaryFrom INTEGER,
                salaryTo INTEGER,
                salaryCurrency TEXT,
                addressId TEXT,
                addressCity TEXT,
                addressStreet TEXT,
                addressBuilding TEXT,
                addressRaw TEXT,
                experienceId TEXT,
                experienceName TEXT,
                scheduleId TEXT,
                scheduleName TEXT,
                employmentId TEXT,
                employmentName TEXT,
                contactsId TEXT,
                contactsName TEXT,
                contactsEmail TEXT,
                contactsPhones TEXT NOT NULL,
                employerId TEXT,
                employerName TEXT,
                employerLogo TEXT,
                areaId INTEGER,
                areaName TEXT,
                skills TEXT NOT NULL,
                url TEXT NOT NULL,
                industryId INTEGER,
                industryName TEXT,
                legacyPayload TEXT,
                addedAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
        """.trimIndent()

        private val MIGRATE_LEGACY_FAVORITES = """
            INSERT INTO favorite_vacancies (
                id,
                name,
                description,
                contactsPhones,
                skills,
                url,
                legacyPayload,
                addedAt
            )
            SELECT
                vacancyId,
                vacancyId,
                '',
                '[]',
                '[]',
                '',
                payload,
                addedAt
            FROM favorite_vacancies_legacy
        """.trimIndent()
    }
}

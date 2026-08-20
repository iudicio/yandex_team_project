package ru.practicum.android.diploma.data.favorites

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.android.diploma.data.db.AppDatabase

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migrationFromOneToTwoPreservesLegacyRow() = runBlocking {
        createVersionOneDatabase()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        val migrated = database.favoriteVacancyDao().findById(LEGACY_ID)

        assertNotNull(migrated)
        assertEquals(LEGACY_ID, migrated?.id)
        assertEquals(LEGACY_ID, migrated?.name)
        assertEquals(LEGACY_PAYLOAD, migrated?.legacyPayload)
        assertEquals(LEGACY_ADDED_AT, migrated?.addedAt)
        assertEquals(emptyList<String>(), migrated?.skills)
        assertEquals(0, migrated?.contactsPhones?.size)
        database.close()
    }

    private fun createVersionOneDatabase() {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DATABASE_NAME)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(CREATE_VERSION_ONE_TABLE)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            )
            .build()
        FrameworkSQLiteOpenHelperFactory().create(configuration).use { helper ->
            helper.writableDatabase.execSQL(
                "INSERT INTO favorite_vacancies (vacancyId, payload, addedAt) VALUES (?, ?, ?)",
                arrayOf<Any>(LEGACY_ID, LEGACY_PAYLOAD, LEGACY_ADDED_AT),
            )
        }
    }

    private companion object {
        const val DATABASE_NAME = "favorite-migration-test.db"
        const val LEGACY_ID = "legacy-vacancy"
        const val LEGACY_PAYLOAD = "{\"source\":\"epic0\"}"
        const val LEGACY_ADDED_AT = 42L

        const val CREATE_VERSION_ONE_TABLE = """
            CREATE TABLE IF NOT EXISTS favorite_vacancies (
                vacancyId TEXT NOT NULL,
                payload TEXT NOT NULL,
                addedAt INTEGER NOT NULL,
                PRIMARY KEY(vacancyId)
            )
        """
    }
}

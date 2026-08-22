package ru.practicum.android.diploma.data.favorites

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.db.entity.FavoritePhoneSnapshot
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity

@RunWith(AndroidJUnit4::class)
class FavoriteVacancyDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteVacancyDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
        ).build()
        dao = database.favoriteVacancyDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fullSnapshotCrudAndFavoriteFlow() = runBlocking {
        val older = entity(id = "older", addedAt = 1L)
        val newer = entity(id = "newer", addedAt = 2L)

        assertFalse(dao.observeIsFavorite(older.id).first())
        dao.add(older)
        dao.add(newer)

        assertTrue(dao.observeIsFavorite(older.id).first())
        assertEquals(older, dao.findById(older.id))
        assertEquals(listOf(newer, older), dao.getAll())
        assertEquals(listOf(newer, older), dao.observeAll().first())

        dao.deleteById(older.id)

        assertFalse(dao.observeIsFavorite(older.id).first())
        assertNull(dao.findById(older.id))
    }
}

private fun entity(id: String, addedAt: Long): FavoriteVacancyEntity = FavoriteVacancyEntity(
    id = id,
    name = "Android-разработчик",
    description = "Описание",
    salaryFrom = 100_000,
    salaryTo = 150_000,
    salaryCurrency = "RUR",
    addressId = "address-1",
    addressCity = "Москва",
    addressStreet = "Льва Толстого",
    addressBuilding = "16",
    addressRaw = "Москва, улица Льва Толстого, 16",
    experienceId = "between1And3",
    experienceName = "От 1 года до 3 лет",
    scheduleId = "remote",
    scheduleName = "Удалённая работа",
    employmentId = "full",
    employmentName = "Полная занятость",
    contactsId = "contacts-1",
    contactsName = "Иван",
    contactsEmail = "ivan@example.com",
    contactsPhones = listOf(FavoritePhoneSnapshot("По будням", "+7 999 000-00-00")),
    employerId = "employer-1",
    employerName = "Компания",
    employerLogo = "https://example.com/logo.png",
    areaId = 1,
    areaName = "Москва",
    skills = listOf("Kotlin", "Room"),
    url = "https://example.com/$id",
    industryId = 7,
    industryName = "IT",
    addedAt = addedAt,
)

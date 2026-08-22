package ru.practicum.android.diploma.data.favorites

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.common.coroutines.DispatcherProvider
import ru.practicum.android.diploma.data.db.dao.FavoriteVacancyDao
import ru.practicum.android.diploma.data.db.entity.FavoriteVacancyEntity
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Industry
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.favorites.FavoritesDatabaseException
import ru.practicum.android.diploma.domain.search.Salary

@OptIn(ExperimentalCoroutinesApi::class)
class RoomFavoriteVacancyRepositoryTest {
    @Test
    fun `repository stores reads observes and removes a full favorite`() = runTest {
        val dao = FakeFavoriteVacancyDao()
        val repository = RoomFavoriteVacancyRepository(
            dao,
            TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        )
        val details = repositoryDetails()

        assertFalse(repository.observeIsFavorite(details.id).first())
        repository.add(details)

        assertTrue(repository.observeIsFavorite(details.id).first())
        assertEquals(details, repository.getFavorite(details.id))
        assertEquals(listOf(details.id), repository.observeFavorites().first().map { vacancy -> vacancy.id })

        repository.remove(details.id)

        assertFalse(repository.observeIsFavorite(details.id).first())
        assertEquals(null, repository.getFavorite(details.id))
    }

    @Test
    fun `dao exception is wrapped in feature database exception`() = runTest {
        val expected = IllegalStateException("database unavailable")
        val dao = FakeFavoriteVacancyDao().apply { operationFailure = expected }
        val repository = RoomFavoriteVacancyRepository(
            dao,
            TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        )

        val thrown = captureFailure { repository.getFavorite("vacancy-1") }

        assertTrue(thrown is FavoritesDatabaseException)
        assertSame(expected, thrown.rootCause())
    }

    @Test
    fun `flow exception is wrapped in feature database exception`() = runTest {
        val expected = IllegalStateException("observe failed")
        val dao = FakeFavoriteVacancyDao().apply { observationFailure = expected }
        val repository = RoomFavoriteVacancyRepository(
            dao,
            TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        )

        val thrown = captureFailure { repository.observeFavorites().first() }

        assertTrue(thrown is FavoritesDatabaseException)
        assertSame(expected, thrown.rootCause())
    }

    @Test
    fun `cancellation is never converted to database failure`() = runTest {
        val expected = CancellationException("cancelled")
        val dao = FakeFavoriteVacancyDao().apply { operationFailure = expected }
        val repository = RoomFavoriteVacancyRepository(
            dao,
            TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        )

        val thrown = captureFailure { repository.getFavorite("vacancy-1") }

        assertTrue(thrown is CancellationException)
        assertEquals(expected.message, thrown.message)
    }
}

private class FakeFavoriteVacancyDao : FavoriteVacancyDao {
    private val entities = MutableStateFlow<List<FavoriteVacancyEntity>>(emptyList())
    var operationFailure: Throwable? = null
    var observationFailure: Throwable? = null

    override suspend fun add(vacancy: FavoriteVacancyEntity) {
        throwOperationFailure()
        entities.value = (entities.value.filterNot { entity -> entity.id == vacancy.id } + vacancy)
            .sortedByDescending(FavoriteVacancyEntity::addedAt)
    }

    override suspend fun deleteById(vacancyId: String) {
        throwOperationFailure()
        entities.value = entities.value.filterNot { entity -> entity.id == vacancyId }
    }

    override fun observeAll(): Flow<List<FavoriteVacancyEntity>> = observationFailure?.let { failure ->
        flow { throw failure }
    } ?: entities

    override suspend fun getAll(): List<FavoriteVacancyEntity> {
        throwOperationFailure()
        return entities.value
    }

    override suspend fun findById(vacancyId: String): FavoriteVacancyEntity? {
        throwOperationFailure()
        return entities.value.firstOrNull { entity -> entity.id == vacancyId }
    }

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = observationFailure?.let { failure ->
        flow { throw failure }
    } ?: entities.map { values -> values.any { entity -> entity.id == vacancyId } }

    private fun throwOperationFailure() {
        operationFailure?.let { failure -> throw failure }
    }
}

private class TestDispatcherProvider(
    override val io: kotlinx.coroutines.CoroutineDispatcher,
) : DispatcherProvider {
    override val default = io
    override val main = io
}

private suspend fun captureFailure(block: suspend () -> Unit): Throwable = try {
    block()
    throw AssertionError("Expected operation to fail")
} catch (throwable: Throwable) {
    assertNotNull(throwable)
    throwable
}

private fun Throwable.rootCause(): Throwable = generateSequence(this) { throwable -> throwable.cause }.last()

private fun repositoryDetails(): VacancyDetailResult = VacancyDetailResult(
    id = "vacancy-1",
    name = "Android-разработчик",
    description = "Описание",
    salary = Salary(from = 100_000, to = null, currency = "RUR"),
    address = Address("address-1", "Москва", "Льва Толстого", "16", "Москва, Льва Толстого, 16"),
    experience = BaseDetailData("between1And3", "От 1 года до 3 лет"),
    schedule = BaseDetailData("remote", "Удалённая работа"),
    employment = BaseDetailData("full", "Полная занятость"),
    contacts = Contacts(
        id = "contacts-1",
        name = "Иван",
        email = "ivan@example.com",
        phones = listOf(Phone("По будням", "+7 999 000-00-00")),
    ),
    employer = Employer("employer-1", "Компания", "https://example.com/logo.png"),
    area = Area(1, "Москва"),
    skills = listOf("Kotlin", "Room"),
    url = "https://example.com/vacancy-1",
    industry = Industry(7, "IT"),
)

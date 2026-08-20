package ru.practicum.android.diploma.domain.details

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.search.VacancyCard

class VacancyDetailsInteractorTest {

    @Test
    fun `network content is returned without reading favorite snapshot`() = runTest {
        val vacancy = vacancyDetailsFixture()
        val favorites = FakeFavoritesInteractor(favorite = vacancy.copy(name = "cached"))
        val interactor = interactor(
            outcome = VacancyDetailsOutcome.Content(vacancy),
            favorites = favorites,
        )

        val result = interactor.getDetails(vacancy.id)

        assertEquals(VacancyDetailsOutcome.Content(vacancy), result)
        assertEquals(0, favorites.getFavoriteCalls)
    }

    @Test
    fun `no internet returns complete favorite snapshot as offline content`() = runTest {
        val cachedVacancy = vacancyDetailsFixture().copy(
            description = "cached full description",
            skills = listOf("Kotlin", "Coroutines"),
        )
        val favorites = FakeFavoritesInteractor(favorite = cachedVacancy)
        val interactor = interactor(
            outcome = failure(VacancyDetailsError.NoInternet),
            favorites = favorites,
        )

        val result = interactor.getDetails(cachedVacancy.id)

        assertEquals(
            VacancyDetailsOutcome.Content(vacancy = cachedVacancy, isOffline = true),
            result,
        )
        assertEquals(1, favorites.getFavoriteCalls)
    }

    @Test
    fun `no internet without favorite keeps no internet error`() = runTest {
        val favorites = FakeFavoritesInteractor(favorite = null)
        val interactor = interactor(
            outcome = failure(VacancyDetailsError.NoInternet),
            favorites = favorites,
        )

        val result = interactor.getDetails("42")

        assertEquals(failure(VacancyDetailsError.NoInternet), result)
    }

    @Test
    fun `not found removes stale Room snapshot before returning error`() = runTest {
        val favorites = FakeFavoritesInteractor(favorite = vacancyDetailsFixture())
        val interactor = interactor(
            outcome = failure(VacancyDetailsError.NotFound),
            favorites = favorites,
        )

        val result = interactor.getDetails("42")

        assertEquals(failure(VacancyDetailsError.NotFound), result)
        assertEquals(listOf("42"), favorites.removedIds)
        assertEquals(null, favorites.favorite)
    }

    @Test
    fun `favorite database failure is exposed as server error`() = runTest {
        val favorites = FakeFavoritesInteractor(
            favorite = vacancyDetailsFixture(),
            getFavoriteFailure = IllegalStateException("Room unavailable"),
        )
        val interactor = interactor(
            outcome = failure(VacancyDetailsError.NoInternet),
            favorites = favorites,
        )

        assertEquals(
            failure(VacancyDetailsError.Server),
            interactor.getDetails("42"),
        )
    }

    @Test
    fun `cancellation while reading favorite is rethrown`() = runTest {
        val cancellation = CancellationException("cancelled")
        val favorites = FakeFavoritesInteractor(
            favorite = null,
            getFavoriteFailure = cancellation,
        )
        val interactor = interactor(
            outcome = failure(VacancyDetailsError.NoInternet),
            favorites = favorites,
        )

        val thrown = try {
            interactor.getDetails("42")
            throw AssertionError("CancellationException was expected")
        } catch (exception: CancellationException) {
            exception
        }

        assertSame(cancellation, thrown)
    }

    private fun interactor(
        outcome: VacancyDetailsOutcome,
        favorites: FakeFavoritesInteractor,
    ): VacancyDetailsInteractor = VacancyDetailsInteractorImpl(
        repository = FakeVacancyDetailRepository(outcome),
        favoritesInteractor = favorites,
    )
}

private class FakeVacancyDetailRepository(
    private val outcome: VacancyDetailsOutcome,
) : VacancyDetailRepository {
    override suspend fun getVacancy(vacancyId: String): VacancyDetailsOutcome = outcome
}

private class FakeFavoritesInteractor(
    var favorite: VacancyDetailResult?,
    private val getFavoriteFailure: Throwable? = null,
) : FavoritesInteractor {
    private val favoriteState = MutableStateFlow(favorite != null)
    var getFavoriteCalls: Int = 0
        private set
    val removedIds = mutableListOf<String>()

    override fun observeFavorites(): Flow<List<VacancyCard>> = flowOf(emptyList())

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = favoriteState

    override suspend fun getFavorite(vacancyId: String): VacancyDetailResult? {
        getFavoriteCalls += 1
        getFavoriteFailure?.let { throw it }
        return favorite?.takeIf { it.id == vacancyId }
    }

    override suspend fun add(vacancy: VacancyDetailResult) {
        favorite = vacancy
        favoriteState.value = true
    }

    override suspend fun remove(vacancyId: String) {
        removedIds += vacancyId
        favorite = favorite?.takeUnless { it.id == vacancyId }
        favoriteState.value = false
    }
}

private fun failure(error: VacancyDetailsError): VacancyDetailsOutcome =
    VacancyDetailsOutcome.Failure(error)

private fun vacancyDetailsFixture(id: String = "42"): VacancyDetailResult = VacancyDetailResult(
    id = id,
    name = "Android developer",
    description = "<p>Build the product</p>",
    salary = null,
    address = Address(
        id = "address-id",
        city = "Moscow",
        street = "Tverskaya",
        building = "1",
        raw = "Moscow, Tverskaya, 1",
    ),
    experience = BaseDetailData(id = "between1And3", name = "1-3 years"),
    schedule = BaseDetailData(id = "remote", name = "Remote"),
    employment = BaseDetailData(id = "full", name = "Full time"),
    contacts = Contacts(
        id = "contacts-id",
        name = "Recruiter",
        email = "jobs@example.com",
        phones = listOf(Phone(comment = "work", formatted = "+7 999 000-00-00")),
    ),
    employer = Employer(id = "employer-id", name = "Company", logo = "https://logo"),
    area = Area(id = 1, name = "Moscow"),
    skills = listOf("Kotlin"),
    url = "https://example.com/vacancy/$id",
    industry = Industry(id = 7, name = "IT"),
)

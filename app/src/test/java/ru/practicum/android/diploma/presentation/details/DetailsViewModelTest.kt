package ru.practicum.android.diploma.presentation.details

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.Area
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Industry
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.details.VacancyDetailsError
import ru.practicum.android.diploma.domain.details.VacancyDetailsInteractor
import ru.practicum.android.diploma.domain.details.VacancyDetailsOutcome
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.search.VacancyCard

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load publishes content and observed favorite status`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vacancy = detailsFixture()
            val favorites = FakeDetailsFavorites(initiallyFavorite = true)
            val interactor = FakeDetailsInteractor(VacancyDetailsOutcome.Content(vacancy))

            val viewModel = DetailsViewModel(vacancy.id, interactor, favorites)
            advanceUntilIdle()

            assertEquals(
                DetailsUiState(
                    isLoading = false,
                    vacancy = vacancy,
                    isOffline = false,
                    isFavorite = true,
                    error = null,
                ),
                viewModel.state.value,
            )
            assertEquals(listOf(vacancy.id), interactor.requestedIds)
        }

    @Test
    fun `cached content preserves offline marker`() = runTest(mainDispatcherRule.dispatcher) {
        val vacancy = detailsFixture()
        val interactor = FakeDetailsInteractor(
            VacancyDetailsOutcome.Content(vacancy = vacancy, isOffline = true),
        )

        val viewModel = DetailsViewModel(
            vacancyId = vacancy.id,
            vacancyDetailsInteractor = interactor,
            favoritesInteractor = FakeDetailsFavorites(initiallyFavorite = true),
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isOffline)
        assertEquals(vacancy, viewModel.state.value.vacancy)
    }

    @Test
    fun `not found remains non favorite when stale Room emission arrives later`() =
        runTest(mainDispatcherRule.dispatcher) {
            val favoriteUpdates = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
            val favorites = FakeDetailsFavorites(favoriteUpdates = favoriteUpdates)
            val viewModel = DetailsViewModel(
                vacancyId = "42",
                vacancyDetailsInteractor = FakeDetailsInteractor(
                    VacancyDetailsOutcome.Failure(VacancyDetailsError.NotFound),
                ),
                favoritesInteractor = favorites,
            )
            advanceUntilIdle()

            favoriteUpdates.emit(true)
            advanceUntilIdle()

            assertEquals(DetailsError.NotFound, viewModel.state.value.error)
            assertFalse(viewModel.state.value.isFavorite)
        }

    @Test
    fun `adding favorite is optimistic and persists full vacancy`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vacancy = detailsFixture()
            val favorites = FakeDetailsFavorites(initiallyFavorite = false)
            val viewModel = DetailsViewModel(
                vacancyId = vacancy.id,
                vacancyDetailsInteractor = FakeDetailsInteractor(VacancyDetailsOutcome.Content(vacancy)),
                favoritesInteractor = favorites,
            )
            advanceUntilIdle()

            viewModel.onFavoriteClicked()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isFavorite)
            assertEquals(listOf(vacancy), favorites.addedVacancies)
        }

    @Test
    fun `removing favorite uses required vacancy id`() = runTest(mainDispatcherRule.dispatcher) {
        val vacancy = detailsFixture()
        val favorites = FakeDetailsFavorites(initiallyFavorite = true)
        val viewModel = DetailsViewModel(
            vacancyId = vacancy.id,
            vacancyDetailsInteractor = FakeDetailsInteractor(VacancyDetailsOutcome.Content(vacancy)),
            favoritesInteractor = favorites,
        )
        advanceUntilIdle()

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)
        assertEquals(listOf(vacancy.id), favorites.removedIds)
    }

    @Test
    fun `database error rolls optimistic favorite back and emits event`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vacancy = detailsFixture()
            val favorites = FakeDetailsFavorites(
                initiallyFavorite = false,
                addFailure = IllegalStateException("Room is full"),
            )
            val viewModel = DetailsViewModel(
                vacancyId = vacancy.id,
                vacancyDetailsInteractor = FakeDetailsInteractor(VacancyDetailsOutcome.Content(vacancy)),
                favoritesInteractor = favorites,
            )
            advanceUntilIdle()

            viewModel.onFavoriteClicked()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isFavorite)
            assertEquals(DetailsEvent.FavoriteUpdateFailed, viewModel.events.first())
        }

    @Test
    fun `share phone and email actions emit safe intent data`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vacancy = detailsFixture()
            val viewModel = DetailsViewModel(
                vacancyId = vacancy.id,
                vacancyDetailsInteractor = FakeDetailsInteractor(VacancyDetailsOutcome.Content(vacancy)),
                favoritesInteractor = FakeDetailsFavorites(),
            )
            advanceUntilIdle()

            viewModel.onShareClicked()
            assertEquals(DetailsEvent.ShareVacancy(vacancy), viewModel.events.first())

            viewModel.onPhoneClicked("  +7 999 000-00-00  ")
            assertEquals(DetailsEvent.DialPhone("+7 999 000-00-00"), viewModel.events.first())

            viewModel.onEmailClicked("  jobs@example.com  ")
            assertEquals(DetailsEvent.SendEmail("jobs@example.com"), viewModel.events.first())
        }

    @Test
    fun `blank required vacancy id fails immediately`() {
        assertThrows(IllegalArgumentException::class.java) {
            DetailsViewModel(
                vacancyId = " ",
                vacancyDetailsInteractor = FakeDetailsInteractor(
                    VacancyDetailsOutcome.Failure(VacancyDetailsError.Server),
                ),
                favoritesInteractor = FakeDetailsFavorites(),
            )
        }
    }
}

private class FakeDetailsInteractor(
    var outcome: VacancyDetailsOutcome,
) : VacancyDetailsInteractor {
    val requestedIds = mutableListOf<String>()

    override suspend fun getDetails(vacancyId: String): VacancyDetailsOutcome {
        requestedIds += vacancyId
        return outcome
    }
}

private class FakeDetailsFavorites(
    initiallyFavorite: Boolean = false,
    private val favoriteUpdates: Flow<Boolean> = MutableStateFlow(initiallyFavorite),
    private val addFailure: Throwable? = null,
    private val removeFailure: Throwable? = null,
) : FavoritesInteractor {
    val addedVacancies = mutableListOf<VacancyDetailResult>()
    val removedIds = mutableListOf<String>()

    override fun observeFavorites(): Flow<List<VacancyCard>> = flowOf(emptyList())

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = favoriteUpdates

    override suspend fun getFavorite(vacancyId: String): VacancyDetailResult? = null

    override suspend fun add(vacancy: VacancyDetailResult) {
        addFailure?.let { throw it }
        addedVacancies += vacancy
        (favoriteUpdates as? MutableStateFlow<Boolean>)?.value = true
    }

    override suspend fun remove(vacancyId: String) {
        removeFailure?.let { throw it }
        removedIds += vacancyId
        (favoriteUpdates as? MutableStateFlow<Boolean>)?.value = false
    }
}

private fun detailsFixture(id: String = "42"): VacancyDetailResult = VacancyDetailResult(
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
    skills = listOf("Kotlin", "Coroutines"),
    url = "https://example.com/vacancy/$id",
    industry = Industry(id = 7, name = "IT"),
)

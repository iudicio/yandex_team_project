package ru.practicum.android.diploma.presentation.favorite

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.presentation.favorites.FavoritesUiState
import ru.practicum.android.diploma.presentation.favorites.FavoritesViewModel
import ru.practicum.android.diploma.presentation.search.MainDispatcherRule
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `empty favorites list exposes Empty state`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = FavoritesViewModel(FakeFavoritesInteractor(favorites = emptyList()))

        Assert.assertEquals(FavoritesUiState.Empty, viewModel.state.awaitLoaded())
    }

    @Test
    fun `non-empty favorites list exposes Content state`() =
        runTest(mainDispatcherRule.dispatcher) {
            val favorites = listOf(
                VacancyCard(id = "1", name = "Android developer"),
                VacancyCard(
                    id = "2",
                    name = "QA engineer",
                    salary = Salary(from = 100_000, currency = "RUR")
                ),
            )
            val viewModel = FavoritesViewModel(FakeFavoritesInteractor(favorites = favorites))

            Assert.assertEquals(FavoritesUiState.Content(favorites), viewModel.state.awaitLoaded())
        }

    @Test
    fun `repository failure exposes DatabaseError`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = FavoritesViewModel(
            FakeFavoritesInteractor(failure = IOException("database unavailable")),
        )

        Assert.assertEquals(FavoritesUiState.DatabaseError, viewModel.state.awaitLoaded())
    }

    private suspend fun StateFlow<FavoritesUiState>.awaitLoaded(): FavoritesUiState =
        first { state -> state != FavoritesUiState.Loading }

    private class FakeFavoritesInteractor(
        private val favorites: List<FavoriteVacancy> = emptyList(),
        private val failure: Throwable? = null,
    ) : FavoritesInteractor {

        override fun observeFavorites(): Flow<List<FavoriteVacancy>> = flow {
            failure?.let { throw it }
            emit(favorites)
        }

        override suspend fun getFavorite(id: String): FavoriteVacancy? =
            favorites.firstOrNull { it.id == id }

        override suspend fun isFavorite(id: String): Boolean = favorites.any { it.id == id }

        override suspend fun add(vacancy: FavoriteVacancy) = Unit

        override suspend fun remove(id: String) = Unit
    }
}

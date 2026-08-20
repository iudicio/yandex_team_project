package ru.practicum.android.diploma.presentation.favorites

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.favorites.FavoritesDatabaseException
import ru.practicum.android.diploma.domain.favorites.FavoritesInteractor
import ru.practicum.android.diploma.domain.search.VacancyCard

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `empty and content emissions update screen state`() = runTest(mainDispatcherRule.dispatcher) {
        val favorites = MutableStateFlow<List<VacancyCard>>(emptyList())
        val viewModel = FavoritesViewModel(FakeFavoritesInteractor(favorites))

        advanceUntilIdle()
        assertEquals(FavoritesUiState.Empty, viewModel.state.value)

        val vacancy = VacancyCard(id = "1", name = "Android-разработчик")
        favorites.value = listOf(vacancy)
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Content(listOf(vacancy)), viewModel.state.value)
    }

    @Test
    fun `database failure exposes dedicated error state`() = runTest(mainDispatcherRule.dispatcher) {
        val failure = FavoritesDatabaseException(IllegalStateException("database unavailable"))
        val failedFlow = flow<List<VacancyCard>> { throw failure }
        val viewModel = FavoritesViewModel(FakeFavoritesInteractor(failedFlow))

        advanceUntilIdle()

        assertEquals(FavoritesUiState.DatabaseError, viewModel.state.value)
    }
}

private class FakeFavoritesInteractor(
    private val favorites: Flow<List<VacancyCard>>,
) : FavoritesInteractor {
    override fun observeFavorites(): Flow<List<VacancyCard>> = favorites

    override fun observeIsFavorite(vacancyId: String): Flow<Boolean> = flow { emit(false) }

    override suspend fun getFavorite(vacancyId: String): VacancyDetailResult? = null

    override suspend fun add(vacancy: VacancyDetailResult) = Unit

    override suspend fun remove(vacancyId: String) = Unit
}

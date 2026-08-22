package ru.practicum.android.diploma.ui.favorites

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.presentation.favorites.FavoritesUiState
import ru.practicum.android.diploma.ui.components.LogoCachePolicy
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.VacancyRow

internal const val FAVORITES_SCREEN_TAG = "favorites_screen"
internal const val FAVORITES_LOADING_TAG = "favorites_loading"
internal const val FAVORITES_EMPTY_TAG = "favorites_empty"
internal const val FAVORITES_ERROR_TAG = "favorites_error"
internal const val FAVORITES_LIST_TAG = "favorites_list"

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onVacancyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(FAVORITES_SCREEN_TAG),
    ) {
        ScreenHeader(title = stringResource(R.string.favorites_title))
        when (state) {
            FavoritesUiState.Loading -> LoadingContent()
            FavoritesUiState.Empty -> PlaceholderContent(
                illustration = R.drawable.favorite_empty_list,
                message = R.string.favorites_empty,
                testTag = FAVORITES_EMPTY_TAG,
            )

            FavoritesUiState.DatabaseError -> PlaceholderContent(
                illustration = R.drawable.search_server_error,
                message = R.string.favorites_database_error,
                testTag = FAVORITES_ERROR_TAG,
            )

            is FavoritesUiState.Content -> FavoritesList(
                vacancies = state.vacancies,
                onVacancyClick = onVacancyClick,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(FAVORITES_LOADING_TAG),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PlaceholderContent(
    @DrawableRes illustration: Int,
    @StringRes message: Int,
    testTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(223.dp),
        )
        Text(
            text = stringResource(message),
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun FavoritesList(
    vacancies: List<VacancyCard>,
    onVacancyClick: (String) -> Unit,
) {
    val missingSalaryText = stringResource(R.string.favorites_salary_not_specified)
    val logoContentDescription = stringResource(R.string.favorites_company_logo)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(FAVORITES_LIST_TAG),
    ) {
        items(
            items = vacancies,
            key = VacancyCard::id,
        ) { vacancy ->
            VacancyRow(
                vacancy = vacancy,
                missingSalaryText = missingSalaryText,
                onClick = { onVacancyClick(vacancy.id) },
                logoCachePolicy = LogoCachePolicy.NetworkOnly,
                logoContentDescription = logoContentDescription,
            )
        }
    }
}

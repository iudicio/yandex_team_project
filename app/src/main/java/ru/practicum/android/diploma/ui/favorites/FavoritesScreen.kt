package ru.practicum.android.diploma.ui.favorites

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.favorites.FavoriteVacancy
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.presentation.favorites.FavoritesUiState
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.components.VacancyRow
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

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
            .testTag(UiTestTags.FAVORITES_SCREEN),
    ) {
        ScreenHeader(title = stringResource(R.string.navigation_favorites))

        when (state) {
            FavoritesUiState.Loading -> LoadingContent()
            FavoritesUiState.Empty -> PlaceholderContent(
                illustration = R.drawable.favorite_empty_list,
                message = R.string.empty_list,
            )

            FavoritesUiState.DatabaseError -> PlaceholderContent(
                illustration = R.drawable.favorite_empty_list,
                message = R.string.favorites_database_error,
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PlaceholderContent(
    @DrawableRes illustration: Int,
    @StringRes message: Int,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(223.dp)
                    .padding(horizontal = 16.dp),
            )
            Text(
                text = stringResource(message),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun FavoritesList(
    vacancies: List<FavoriteVacancy>,
    onVacancyClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = vacancies,
            key = FavoriteVacancy::id,
        ) { vacancy ->
            VacancyRow(
                vacancy = vacancy,
                onClick = { onVacancyClick(vacancy.id) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesEmptyPreview() {
    DiplomaTheme {
        FavoritesScreen(
            state = FavoritesUiState.Empty,
            onVacancyClick = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesDarkPreview() {
    DiplomaTheme {
        FavoritesScreen(
            state = FavoritesUiState.Content(
                vacancies = listOf(
                    FavoriteVacancy(
                        id = "1",
                        name = "Android developer",
                        company = "Company",
                        city = "Moscow",
                        salary = Salary(from = 150_000, currency = "RUR"),
                        logo = null,
                    ),
                ),
            ),
            onVacancyClick = {},
        )
    }
}

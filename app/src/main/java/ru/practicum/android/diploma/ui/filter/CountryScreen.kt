package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.presentation.filter.CatalogLoadState
import ru.practicum.android.diploma.presentation.filter.CountryUiState
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
fun CountryScreen(
    state: CountryUiState,
    onBackClicked: () -> Unit,
    onCountryClicked: (AreaSelection) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.COUNTRY_SCREEN),
    ) {
        FilterTopBar(
            title = stringResource(R.string.filter_country_title),
            onBackClicked = onBackClicked,
        )
        when (state.loadState) {
            CatalogLoadState.LOADING -> CatalogLoading(Modifier.weight(1f))
            CatalogLoadState.ERROR -> CatalogMessage(
                illustration = R.drawable.filter_catalog_error,
                message = stringResource(R.string.filter_catalog_error),
                modifier = Modifier.weight(1f),
                onClick = onRetryClicked,
            )
            CatalogLoadState.CONTENT -> LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag(UiTestTags.COUNTRY_LIST),
            ) {
                items(state.countries, key = AreaSelection::id) { country ->
                    CatalogListRow(
                        text = country.name,
                        onClick = { onCountryClicked(country) },
                        selected = country.id == state.selectedCountryId,
                    )
                }
            }
        }
    }
}

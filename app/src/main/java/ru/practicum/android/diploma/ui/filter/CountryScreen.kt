package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
internal fun CountryScreen(
    state: CountryScreenModel,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCountryClick: (FilterCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.COUNTRY_SCREEN),
    ) {
        FilterHeader(
            title = stringResource(R.string.screen_country),
            onBack = onBack,
        )
        FilterCatalogBody(
            state = state.content,
            errorMessage = stringResource(R.string.filter_countries_load_error),
            emptyMessage = stringResource(R.string.filter_country_not_found),
            listTestTag = UiTestTags.COUNTRY_LIST,
            rowStyle = FilterCatalogRowStyle.Chevron,
            onRetry = onRetry,
            onItemClick = onCountryClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

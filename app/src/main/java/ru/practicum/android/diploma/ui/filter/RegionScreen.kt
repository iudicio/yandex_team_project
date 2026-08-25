package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
internal fun RegionScreen(
    state: RegionScreenModel,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onRegionClick: (FilterCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.REGION_SCREEN),
    ) {
        FilterHeader(
            title = stringResource(R.string.screen_region),
            onBack = onBack,
        )
        FilterSearchField(
            query = state.query,
            hint = stringResource(R.string.filter_region_search_hint),
            onQueryChanged = onQueryChanged,
            onClear = onClearQuery,
            searchDescription = stringResource(R.string.filter_start_search_description),
            clearDescription = stringResource(R.string.filter_clear_search_description),
            testTag = UiTestTags.REGION_SEARCH,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FILTER_SCREEN_PADDING),
        )
        FilterCatalogBody(
            state = state.content,
            errorMessage = stringResource(R.string.filter_regions_load_error),
            emptyMessage = stringResource(R.string.filter_region_not_found),
            listTestTag = UiTestTags.REGION_LIST,
            rowStyle = FilterCatalogRowStyle.Chevron,
            onRetry = onRetry,
            onItemClick = onRegionClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

private val FILTER_SCREEN_PADDING = 16.dp

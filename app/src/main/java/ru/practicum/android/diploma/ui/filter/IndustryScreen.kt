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
internal fun IndustryScreen(
    state: IndustryScreenModel,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onIndustryClick: (FilterCatalogItem) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.INDUSTRY_SCREEN),
    ) {
        FilterHeader(
            title = stringResource(R.string.screen_industry),
            onBack = onBack,
        )
        FilterSearchField(
            query = state.query,
            hint = stringResource(R.string.filter_industry_search_hint),
            onQueryChanged = onQueryChanged,
            onClear = onClearQuery,
            searchDescription = stringResource(R.string.filter_start_search_description),
            clearDescription = stringResource(R.string.filter_clear_search_description),
            testTag = UiTestTags.INDUSTRY_SEARCH,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FILTER_SCREEN_PADDING),
        )
        FilterCatalogBody(
            state = state.content,
            errorMessage = stringResource(R.string.filter_industries_load_error),
            emptyMessage = stringResource(R.string.filter_industry_not_found),
            listTestTag = UiTestTags.INDUSTRY_LIST,
            rowStyle = FilterCatalogRowStyle.Radio,
            onRetry = onRetry,
            onItemClick = onIndustryClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        if (state.hasSelection) {
            FilterPrimaryButton(
                text = stringResource(R.string.filter_select),
                onClick = onConfirm,
                testTag = UiTestTags.INDUSTRY_SELECT,
                modifier = Modifier.padding(FILTER_SCREEN_PADDING),
            )
        }
    }
}

private val FILTER_SCREEN_PADDING = 16.dp

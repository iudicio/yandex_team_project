package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.filter.RegionSelection
import ru.practicum.android.diploma.presentation.filter.CatalogLoadState
import ru.practicum.android.diploma.presentation.filter.RegionUiState
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
fun RegionScreen(
    state: RegionUiState,
    onBackClicked: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onRegionClicked: (RegionSelection) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.REGION_SCREEN),
    ) {
        FilterTopBar(
            title = stringResource(R.string.filter_region_title),
            onBackClicked = onBackClicked,
        )
        CatalogSearchField(
            query = state.query,
            hint = stringResource(R.string.filter_region_search_hint),
            onQueryChanged = onQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp),
            testTag = UiTestTags.REGION_SEARCH,
        )
        when (state.loadState) {
            CatalogLoadState.LOADING -> CatalogLoading(Modifier.weight(1f))
            CatalogLoadState.ERROR -> CatalogMessage(
                illustration = R.drawable.filter_catalog_error,
                message = stringResource(R.string.filter_catalog_error),
                modifier = Modifier.weight(1f),
                onClick = onRetryClicked,
            )
            CatalogLoadState.CONTENT -> if (state.visibleRegions.isEmpty()) {
                CatalogMessage(
                    illustration = R.drawable.filter_region_not_found,
                    message = stringResource(R.string.filter_region_not_found),
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                        .testTag(UiTestTags.REGION_LIST),
                ) {
                    items(state.visibleRegions, key = RegionSelection::id) { region ->
                        CatalogListRow(
                            text = region.name,
                            onClick = { onRegionClicked(region) },
                            selected = region.id == state.selectedRegionId,
                        )
                    }
                }
            }
        }
    }
}

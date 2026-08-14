package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.presentation.filter.CatalogLoadState
import ru.practicum.android.diploma.presentation.filter.IndustryUiState
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
fun IndustryScreen(
    state: IndustryUiState,
    onBackClicked: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onIndustryClicked: (Industry) -> Unit,
    onChooseClicked: (Industry) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.INDUSTRY_SCREEN),
    ) {
        FilterTopBar(
            title = stringResource(R.string.filter_industry_title),
            onBackClicked = onBackClicked,
        )
        CatalogSearchField(
            query = state.query,
            hint = stringResource(R.string.filter_industry_search_hint),
            onQueryChanged = onQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp),
            testTag = UiTestTags.INDUSTRY_SEARCH,
        )
        when (state.loadState) {
            CatalogLoadState.LOADING -> CatalogLoading(Modifier.weight(1f))
            CatalogLoadState.ERROR -> CatalogMessage(
                illustration = R.drawable.filter_catalog_error,
                message = stringResource(R.string.filter_catalog_error),
                modifier = Modifier.weight(1f),
                onClick = onRetryClicked,
            )
            CatalogLoadState.CONTENT -> if (state.visibleIndustries.isEmpty()) {
                CatalogMessage(
                    illustration = R.drawable.filter_region_not_found,
                    message = stringResource(R.string.filter_industry_not_found),
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                        .testTag(UiTestTags.INDUSTRY_LIST),
                ) {
                    items(state.visibleIndustries, key = Industry::id) { industry ->
                        CatalogListRow(
                            text = industry.name,
                            onClick = { onIndustryClicked(industry) },
                            trailingContent = {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RadioButton(
                                        selected = industry.id == state.selectedIndustryId,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.primary,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        state.selectedIndustry?.let { selectedIndustry ->
            FilterPrimaryButton(
                text = stringResource(R.string.filter_choose),
                onClick = { onChooseClicked(selectedIndustry) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag(UiTestTags.INDUSTRY_CHOOSE),
            )
        }
    }
}

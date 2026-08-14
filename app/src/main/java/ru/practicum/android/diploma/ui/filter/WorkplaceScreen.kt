package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.filter.WorkplaceUiState
import ru.practicum.android.diploma.ui.components.UiTestTags

@Composable
fun WorkplaceScreen(
    state: WorkplaceUiState,
    onBackClicked: () -> Unit,
    onCountryClicked: () -> Unit,
    onCountryClearClicked: () -> Unit,
    onRegionClicked: () -> Unit,
    onRegionClearClicked: () -> Unit,
    onChooseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.WORKPLACE_SCREEN),
    ) {
        FilterTopBar(
            title = stringResource(R.string.filter_workplace_title),
            onBackClicked = onBackClicked,
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            FilterSelectionRow(
                label = stringResource(R.string.filter_country),
                value = state.countryName,
                onClick = onCountryClicked,
                onClearClicked = if (state.countryName == null) null else onCountryClearClicked,
                clearDescription = stringResource(R.string.filter_clear_country_description),
                modifier = Modifier.testTag(UiTestTags.WORKPLACE_COUNTRY),
            )
            FilterSelectionRow(
                label = stringResource(R.string.filter_region),
                value = state.regionName,
                onClick = onRegionClicked,
                onClearClicked = if (state.regionName == null) null else onRegionClearClicked,
                clearDescription = stringResource(R.string.filter_clear_region_description),
                modifier = Modifier.testTag(UiTestTags.WORKPLACE_REGION),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (state.canChoose) {
            FilterPrimaryButton(
                text = stringResource(R.string.filter_choose),
                onClick = onChooseClicked,
                modifier = Modifier
                    .padding(16.dp)
                    .testTag(UiTestTags.WORKPLACE_CHOOSE),
            )
        }
    }
}

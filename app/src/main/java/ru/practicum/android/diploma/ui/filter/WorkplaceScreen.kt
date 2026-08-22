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
internal fun WorkplaceScreen(
    state: WorkplaceScreenModel,
    onBack: () -> Unit,
    onCountryClick: () -> Unit,
    onCountryClear: () -> Unit,
    onRegionClick: () -> Unit,
    onRegionClear: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.WORKPLACE_SCREEN),
    ) {
        FilterHeader(
            title = stringResource(R.string.screen_workplace),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = FILTER_SCREEN_PADDING),
        ) {
            FilterSettingRow(
                title = stringResource(R.string.filter_country),
                value = state.countryName,
                onClick = onCountryClick,
                onClear = if (state.countryName == null) null else onCountryClear,
                actionDescription = stringResource(R.string.filter_open_country_description),
                clearDescription = stringResource(R.string.filter_clear_country_description),
                testTag = UiTestTags.WORKPLACE_COUNTRY,
            )
            FilterSettingRow(
                title = stringResource(R.string.filter_region),
                value = state.regionName,
                onClick = onRegionClick,
                onClear = if (state.regionName == null) null else onRegionClear,
                actionDescription = stringResource(R.string.filter_open_region_description),
                clearDescription = stringResource(R.string.filter_clear_region_description),
                testTag = UiTestTags.WORKPLACE_REGION,
                modifier = Modifier.padding(top = FILTER_SCREEN_PADDING),
            )
        }
        if (state.hasSelection) {
            FilterPrimaryButton(
                text = stringResource(R.string.filter_select),
                onClick = onConfirm,
                testTag = UiTestTags.WORKPLACE_SELECT,
                modifier = Modifier.padding(FILTER_SCREEN_PADDING),
            )
        }
    }
}

private val FILTER_SCREEN_PADDING = 16.dp

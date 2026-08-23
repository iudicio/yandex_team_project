package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.theme.FigmaSearchHint
import ru.practicum.android.diploma.ui.theme.FigmaSecondary
import ru.practicum.android.diploma.ui.theme.FigmaSurfaceLight
import ru.practicum.android.diploma.ui.theme.SearchFieldContent

@Composable
internal fun FilterScreen(
    state: FilterScreenModel,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onWorkplaceClick: () -> Unit,
    onWorkplaceClear: () -> Unit,
    onIndustryClick: () -> Unit,
    onIndustryClear: () -> Unit,
    onSalaryChanged: (String) -> Unit,
    onOnlyWithSalaryChanged: (Boolean) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .testTag(UiTestTags.FILTER_SCREEN),
    ) {
        FilterHeader(
            title = stringResource(R.string.filter_title),
            onBack = onBack,
        )
        FilterFields(
            state = state,
            onWorkplaceClick = onWorkplaceClick,
            onWorkplaceClear = onWorkplaceClear,
            onIndustryClick = onIndustryClick,
            onIndustryClear = onIndustryClear,
            onSalaryChanged = onSalaryChanged,
            onOnlyWithSalaryChanged = onOnlyWithSalaryChanged,
            modifier = Modifier.weight(1f),
        )
        FilterActions(
            hasActiveFilters = state.hasActiveFilters,
            canApply = state.canApply,
            onApply = onApply,
            onReset = onReset,
        )
    }
}

@Composable
private fun FilterFields(
    state: FilterScreenModel,
    onWorkplaceClick: () -> Unit,
    onWorkplaceClear: () -> Unit,
    onIndustryClick: () -> Unit,
    onIndustryClear: () -> Unit,
    onSalaryChanged: (String) -> Unit,
    onOnlyWithSalaryChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FILTER_SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(FILTER_SCREEN_GAP),
    ) {
        FilterSettingRow(
            title = stringResource(R.string.filter_workplace),
            value = state.workplaceName,
            onClick = onWorkplaceClick,
            onClear = if (state.workplaceName == null) null else onWorkplaceClear,
            actionDescription = stringResource(R.string.filter_open_workplace_description),
            clearDescription = stringResource(R.string.filter_clear_workplace_description),
            testTag = UiTestTags.FILTER_WORKPLACE,
        )
        FilterSettingRow(
            title = stringResource(R.string.filter_industry),
            value = state.industryName,
            onClick = onIndustryClick,
            onClear = if (state.industryName == null) null else onIndustryClear,
            actionDescription = stringResource(R.string.filter_open_industry_description),
            clearDescription = stringResource(R.string.filter_clear_industry_description),
            testTag = UiTestTags.FILTER_INDUSTRY,
        )
        SalaryField(
            salary = state.salaryInput,
            onSalaryChanged = onSalaryChanged,
        )
        SalaryCheckbox(
            checked = state.onlyWithSalary,
            onCheckedChange = onOnlyWithSalaryChanged,
        )
    }
}

@Composable
private fun FilterActions(
    hasActiveFilters: Boolean,
    canApply: Boolean,
    onApply: () -> Unit,
    onReset: () -> Unit,
) {
    val showApply = hasActiveFilters || canApply
    if (!showApply) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = FILTER_SCREEN_PADDING,
                end = FILTER_SCREEN_PADDING,
                bottom = FILTER_SCREEN_PADDING,
            ),
    ) {
        FilterPrimaryButton(
            text = stringResource(R.string.filter_apply),
            onClick = onApply,
            testTag = UiTestTags.FILTER_APPLY,
        )
        if (hasActiveFilters) {
            TextButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiTestTags.FILTER_RESET),
            ) {
                Text(
                    text = stringResource(R.string.filter_reset),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun SalaryField(
    salary: String,
    onSalaryChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FILTER_FIELD_HEIGHT)
            .clip(RoundedCornerShape(FILTER_CORNER_RADIUS))
            .background(if (isSystemInDarkTheme()) FigmaSecondary else FigmaSurfaceLight)
            .padding(start = FILTER_SCREEN_PADDING, end = FILTER_ICON_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = salary,
            onValueChange = { value -> onSalaryChanged(value.filter(Char::isDigit)) },
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .testTag(UiTestTags.FILTER_SALARY_INPUT),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = SearchFieldContent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (salary.isEmpty()) {
                        Text(
                            text = stringResource(R.string.filter_salary_hint),
                            color = FigmaSearchHint,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        Column {
                            Text(
                                text = stringResource(R.string.filter_salary),
                                color = SearchFieldContent,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            innerTextField()
                        }
                    }
                    if (salary.isEmpty()) {
                        innerTextField()
                    }
                }
            },
        )
        if (salary.isNotEmpty()) {
            IconButton(
                onClick = { onSalaryChanged("") },
                modifier = Modifier
                    .size(FILTER_TOUCH_SIZE)
                    .testTag(UiTestTags.FILTER_SALARY_CLEAR),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.filter_clear_salary_description),
                    tint = SearchFieldContent,
                )
            }
        }
    }
}

@Composable
private fun SalaryCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FILTER_CHECKBOX_ROW_HEIGHT)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = FILTER_CHECKBOX_VERTICAL_PADDING)
            .testTag(UiTestTags.FILTER_ONLY_WITH_SALARY),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.filter_only_with_salary),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

private val FILTER_SCREEN_PADDING = 16.dp
private val FILTER_SCREEN_GAP = 16.dp
private val FILTER_FIELD_HEIGHT = 56.dp
private val FILTER_CHECKBOX_ROW_HEIGHT = 48.dp
private val FILTER_CHECKBOX_VERTICAL_PADDING = 4.dp
private val FILTER_TOUCH_SIZE = 48.dp
private val FILTER_CORNER_RADIUS = 12.dp
private val FILTER_ICON_PADDING = 4.dp

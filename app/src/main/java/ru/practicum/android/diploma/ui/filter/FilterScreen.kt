package ru.practicum.android.diploma.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.filter.FilterUiState
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

@Composable
fun FilterScreen(
    state: FilterUiState,
    onBackClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onWorkplaceClicked: () -> Unit,
    onWorkplaceClearClicked: () -> Unit,
    onIndustryClicked: () -> Unit,
    onIndustryClearClicked: () -> Unit,
    onSalaryChanged: (String) -> Unit,
    onSalaryClearClicked: () -> Unit,
    onOnlyWithSalaryChanged: (Boolean) -> Unit,
    onApplyClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .testTag(UiTestTags.FILTER_SCREEN),
    ) {
        FilterTopBar(
            title = stringResource(R.string.filter_title),
            onBackClicked = onBackClicked,
            backTestTag = UiTestTags.FILTER_BACK,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            FilterSelectionRow(
                label = stringResource(R.string.filter_workplace),
                value = state.workplaceName,
                onClick = onWorkplaceClicked,
                onClearClicked = if (state.workplaceName == null) null else onWorkplaceClearClicked,
                clearDescription = stringResource(R.string.filter_clear_workplace_description),
                modifier = Modifier.testTag(UiTestTags.FILTER_WORKPLACE),
            )
            FilterSelectionRow(
                label = stringResource(R.string.filter_industry),
                value = state.industryName,
                onClick = onIndustryClicked,
                onClearClicked = if (state.industryName == null) null else onIndustryClearClicked,
                clearDescription = stringResource(R.string.filter_clear_industry_description),
                modifier = Modifier.testTag(UiTestTags.FILTER_INDUSTRY),
            )
            Spacer(modifier = Modifier.height(24.dp))
            SalaryField(
                salary = state.salary,
                onSalaryChanged = onSalaryChanged,
                onClearClicked = onSalaryClearClicked,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OnlyWithSalaryRow(
                checked = state.onlyWithSalary,
                onCheckedChange = onOnlyWithSalaryChanged,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.hasActiveFilters) {
            FilterPrimaryButton(
                text = stringResource(R.string.filter_apply),
                onClick = onApplyClicked,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag(UiTestTags.FILTER_APPLY),
            )
            TextButton(
                onClick = onResetClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
    onClearClicked: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val labelColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    BasicTextField(
        value = salary,
        onValueChange = onSalaryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, end = 4.dp)
            .testTag(UiTestTags.FILTER_SALARY),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.filter_salary_label),
                        color = labelColor,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (salary.isEmpty()) {
                            Text(
                                text = stringResource(R.string.filter_salary_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                }
                if (salary.isNotEmpty()) {
                    IconButton(
                        onClick = onClearClicked,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag(UiTestTags.FILTER_SALARY_CLEAR),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.filter_clear_salary_description),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun OnlyWithSalaryRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
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
                uncheckedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterScreenPreview() {
    DiplomaTheme {
        FilterScreen(
            state = FilterUiState(
                countryId = 113,
                countryName = "Россия",
                regionId = 1,
                regionName = "Москва",
                industryId = "7",
                industryName = "IT",
                salary = "40000",
                onlyWithSalary = true,
            ),
            onBackClicked = {},
            onResetClicked = {},
            onWorkplaceClicked = {},
            onWorkplaceClearClicked = {},
            onIndustryClicked = {},
            onIndustryClearClicked = {},
            onSalaryChanged = {},
            onSalaryClearClicked = {},
            onOnlyWithSalaryChanged = {},
            onApplyClicked = {},
        )
    }
}

package ru.practicum.android.diploma.ui.filter

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.theme.FigmaSearchHint
import ru.practicum.android.diploma.ui.theme.FigmaSecondary
import ru.practicum.android.diploma.ui.theme.FigmaSurfaceLight
import ru.practicum.android.diploma.ui.theme.SearchFieldContent

internal data class FilterCatalogItem(
    val id: String,
    val name: String,
    val isSelected: Boolean,
)

internal sealed interface FilterCatalogContent {
    data object Loading : FilterCatalogContent

    data class Items(val values: List<FilterCatalogItem>) : FilterCatalogContent

    data object Empty : FilterCatalogContent

    data object NoInternet : FilterCatalogContent

    data object GenericError : FilterCatalogContent
}

internal enum class FilterCatalogRowStyle {
    Radio,
    Chevron,
}

@Composable
internal fun FilterHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    ScreenHeader(
        title = title,
        modifier = modifier,
        backActions = {
            IconButton(onClick = onBack, modifier = Modifier.size(FILTER_TOUCH_SIZE)) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.filter_back_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = actions,
    )
}

@Composable
internal fun FilterSettingRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
    onClear: (() -> Unit)?,
    actionDescription: String,
    clearDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(FILTER_SETTING_ROW_HEIGHT)
            .clickable(onClick = onClick)
            .padding(start = FILTER_HORIZONTAL_PADDING, end = FILTER_ICON_PADDING)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (value == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                style = if (value == null) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
            value?.let { selectedValue ->
                Text(
                    text = selectedValue,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
            }
        }
        if (value == null || onClear == null) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = actionDescription,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(FILTER_ICON_PADDING),
            )
        } else {
            IconButton(onClick = onClear, modifier = Modifier.size(FILTER_TOUCH_SIZE)) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = clearDescription,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
internal fun FilterSearchField(
    query: String,
    hint: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    searchDescription: String,
    clearDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Row(
        modifier = modifier
            .height(FILTER_FIELD_HEIGHT)
            .clip(RoundedCornerShape(FILTER_CORNER_RADIUS))
            .background(filterFieldBackground())
            .padding(start = FILTER_HORIZONTAL_PADDING, end = FILTER_ICON_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .testTag(testTag),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = SearchFieldContent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            color = FigmaSearchHint,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    innerTextField()
                }
            },
        )
        IconButton(
            onClick = onClear,
            enabled = query.isNotEmpty(),
            modifier = Modifier.size(FILTER_TOUCH_SIZE),
        ) {
            Icon(
                painter = painterResource(if (query.isEmpty()) R.drawable.ic_search else R.drawable.ic_close),
                contentDescription = if (query.isEmpty()) searchDescription else clearDescription,
                tint = SearchFieldContent,
            )
        }
    }
}

@Composable
internal fun FilterPrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(FILTER_BUTTON_HEIGHT)
            .testTag(testTag),
        shape = RoundedCornerShape(FILTER_CORNER_RADIUS),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun FilterCatalogBody(
    state: FilterCatalogContent,
    errorMessage: String,
    emptyMessage: String,
    listTestTag: String,
    rowStyle: FilterCatalogRowStyle,
    onRetry: () -> Unit,
    onItemClick: (FilterCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        FilterCatalogContent.Loading -> FilterCatalogLoading(modifier)
        is FilterCatalogContent.Items -> FilterCatalogList(
            items = state.values,
            onItemClick = onItemClick,
            testTag = listTestTag,
            rowStyle = rowStyle,
            modifier = modifier,
        )
        FilterCatalogContent.Empty -> FilterCatalogMessage(
            image = R.drawable.filter_region_not_found,
            message = emptyMessage,
            testTag = UiTestTags.FILTER_CATALOG_EMPTY,
            modifier = modifier,
        )
        FilterCatalogContent.NoInternet -> FilterCatalogMessage(
            image = R.drawable.search_no_internet,
            message = stringResource(R.string.filter_no_internet),
            testTag = UiTestTags.FILTER_CATALOG_ERROR,
            modifier = modifier,
            onRetry = onRetry,
        )
        FilterCatalogContent.GenericError -> FilterCatalogMessage(
            image = R.drawable.filter_catalog_error,
            message = errorMessage,
            testTag = UiTestTags.FILTER_CATALOG_ERROR,
            modifier = modifier,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun FilterCatalogLoading(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(UiTestTags.FILTER_CATALOG_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun FilterCatalogList(
    items: List<FilterCatalogItem>,
    onItemClick: (FilterCatalogItem) -> Unit,
    testTag: String,
    rowStyle: FilterCatalogRowStyle,
    modifier: Modifier,
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(testTag),
    ) {
        items(
            count = items.size,
            key = { index -> items[index].id },
        ) { index ->
            FilterCatalogRow(
                item = items[index],
                rowStyle = rowStyle,
                onClick = onItemClick,
            )
        }
    }
}

@Composable
private fun FilterCatalogRow(
    item: FilterCatalogItem,
    rowStyle: FilterCatalogRowStyle,
    onClick: (FilterCatalogItem) -> Unit,
) {
    val contentColor = if (item.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .heightIn(min = FILTER_LIST_ROW_HEIGHT)
            .padding(
                start = FILTER_HORIZONTAL_PADDING,
                top = FILTER_LIST_ROW_VERTICAL_PADDING,
                end = FILTER_ICON_PADDING,
                bottom = FILTER_LIST_ROW_VERTICAL_PADDING,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
        )
        FilterCatalogSelection(
            rowStyle = rowStyle,
            isSelected = item.isSelected,
            contentColor = contentColor,
        )
    }
}

@Composable
private fun FilterCatalogSelection(
    rowStyle: FilterCatalogRowStyle,
    isSelected: Boolean,
    contentColor: Color,
) {
    when (rowStyle) {
        FilterCatalogRowStyle.Radio -> RadioButton(
            selected = isSelected,
            onClick = null,
        )
        FilterCatalogRowStyle.Chevron -> Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.padding(FILTER_ICON_PADDING),
        )
    }
}

@Composable
private fun FilterCatalogMessage(
    @DrawableRes image: Int,
    message: String,
    testTag: String,
    modifier: Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(FILTER_HORIZONTAL_PADDING)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(FILTER_PLACEHOLDER_HEIGHT),
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = FILTER_HORIZONTAL_PADDING),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        onRetry?.let { retry ->
            Button(
                onClick = retry,
                modifier = Modifier
                    .padding(top = FILTER_VERTICAL_GAP)
                    .testTag(UiTestTags.FILTER_CATALOG_RETRY),
            ) {
                Text(text = stringResource(R.string.filter_retry))
            }
        }
    }
}

private val FILTER_TOUCH_SIZE = 48.dp
private val FILTER_FIELD_HEIGHT = 56.dp
private val FILTER_SETTING_ROW_HEIGHT = 60.dp
private val FILTER_BUTTON_HEIGHT = 60.dp
private val FILTER_LIST_ROW_HEIGHT = 56.dp
private val FILTER_LIST_ROW_VERTICAL_PADDING = 12.dp
private val FILTER_PLACEHOLDER_HEIGHT = 223.dp
private val FILTER_CORNER_RADIUS = 12.dp
private val FILTER_HORIZONTAL_PADDING = 16.dp
private val FILTER_ICON_PADDING = 4.dp
private val FILTER_VERTICAL_GAP = 24.dp

@Composable
private fun filterFieldBackground() = if (isSystemInDarkTheme()) FigmaSecondary else FigmaSurfaceLight

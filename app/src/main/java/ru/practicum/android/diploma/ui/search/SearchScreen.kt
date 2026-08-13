package ru.practicum.android.diploma.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.search.SearchAction
import ru.practicum.android.diploma.presentation.search.SearchUiState
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

@Composable
fun SearchScreen(
    state: SearchUiState,
    focusRequestKey: Int,
    onQueryChanged: (String) -> Unit,
    onSearchActionClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequestKey) {
        if (focusRequestKey > 0) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.SEARCH_SCREEN),
    ) {
        ScreenHeader(
            title = stringResource(R.string.search_title),
            actions = {
                IconButton(
                    onClick = onFilterClicked,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(48.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter),
                        contentDescription = stringResource(R.string.filter_description),
                        tint = if (hasActiveFilters) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                }
            },
        )

        SearchField(
            state = state,
            focusRequester = focusRequester,
            onQueryChanged = onQueryChanged,
            onSearchActionClicked = onSearchActionClicked,
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .fillMaxWidth(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.search_initial_illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(223.dp)
                    .padding(horizontal = 16.dp)
                    .testTag(UiTestTags.SEARCH_INITIAL_ILLUSTRATION),
            )
        }
    }
}

@Composable
private fun SearchField(
    state: SearchUiState,
    focusRequester: FocusRequester,
    onQueryChanged: (String) -> Unit,
    onSearchActionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionIcon = when (state.action) {
        SearchAction.SEARCH -> R.drawable.ic_search
        SearchAction.CLEAR -> R.drawable.ic_close
    }
    val actionDescription = when (state.action) {
        SearchAction.SEARCH -> R.string.search_action_description
        SearchAction.CLEAR -> R.string.clear_search_description
    }

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 16.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag(UiTestTags.SEARCH_INPUT),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchActionClicked() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (state.query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    innerTextField()
                }
            },
        )
        IconButton(
            onClick = onSearchActionClicked,
            modifier = Modifier
                .size(48.dp)
                .testTag(UiTestTags.SEARCH_ACTION),
        ) {
            Icon(
                painter = painterResource(actionIcon),
                contentDescription = stringResource(actionDescription),
                tint = colorResource(R.color.search_icon),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    DiplomaTheme {
        SearchScreen(
            state = SearchUiState(query = ""),
            focusRequestKey = 0,
            onQueryChanged = {},
            onSearchActionClicked = {},
            onFilterClicked = {},
        )
    }
}

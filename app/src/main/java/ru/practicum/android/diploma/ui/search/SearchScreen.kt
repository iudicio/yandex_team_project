package ru.practicum.android.diploma.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImagePainter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.format
import ru.practicum.android.diploma.presentation.search.SearchAction
import ru.practicum.android.diploma.presentation.search.SearchResultUiState
import ru.practicum.android.diploma.presentation.search.SearchUiState
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.components.VacancyRow
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

@Composable
fun SearchScreen(
    state: SearchUiState,
    focusRequestKey: Int,
    onQueryChanged: (String) -> Unit,
    onSearchActionClicked: () -> Unit,
    onSearchSubmitted: () -> Unit,
    onFilterClicked: () -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
    onVacancyClicked: (String) -> Unit = {},
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
        SearchHeader(
            hasActiveFilters = hasActiveFilters,
            onFilterClicked = onFilterClicked,
        )
        SearchField(
            state = state,
            focusRequester = focusRequester,
            onQueryChanged = onQueryChanged,
            onSearchActionClicked = onSearchActionClicked,
            onSearchSubmitted = {
                keyboardController?.hide()
                onSearchSubmitted()
            },
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
        )
        SearchResult(
            result = state.result,
            onLoadNextPage = onLoadNextPage,
            onVacancyClicked = onVacancyClicked,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun SearchHeader(
    hasActiveFilters: Boolean,
    onFilterClicked: () -> Unit,
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
                    painter = painterResource(
                        if (hasActiveFilters) R.drawable.ic_filter_active else R.drawable.ic_filter,
                    ),
                    contentDescription = stringResource(R.string.filter_description),
                    tint = if (hasActiveFilters) {
                        androidx.compose.ui.graphics.Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    )
}

@Composable
private fun SearchResult(
    result: SearchResultUiState,
    onLoadNextPage: () -> Unit,
    onVacancyClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (result) {
        SearchResultUiState.Initial -> InitialResult(modifier)
        SearchResultUiState.Loading -> LoadingResult(modifier)
        is SearchResultUiState.Content -> ContentResult(
            result = result,
            onLoadNextPage = onLoadNextPage,
            onVacancyClicked = onVacancyClicked,
            modifier = modifier,
        )
        SearchResultUiState.Empty -> PlaceholderResult(
            image = R.drawable.search_empty_results,
            text = stringResource(R.string.search_results_error),
            chip = stringResource(R.string.search_empty_results),
            testTag = UiTestTags.SEARCH_EMPTY,
            modifier = modifier,
        )
        is SearchResultUiState.NoInternet -> PlaceholderResult(
            image = R.drawable.search_no_internet,
            text = stringResource(R.string.search_no_internet),
            testTag = UiTestTags.SEARCH_NO_INTERNET,
            modifier = modifier,
        )
        is SearchResultUiState.Error -> PlaceholderResult(
            image = R.drawable.search_server_error,
            text = stringResource(R.string.search_server_error),
            testTag = UiTestTags.SEARCH_ERROR,
            modifier = modifier,
        )
    }
}

@Composable
private fun InitialResult(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
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

@Composable
private fun LoadingResult(modifier: Modifier) {
    Box(
        modifier = modifier.testTag(UiTestTags.SEARCH_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .padding(6.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )
    }
}

@Composable
private fun PlaceholderResult(
    image: Int,
    text: String,
    testTag: String,
    modifier: Modifier = Modifier,
    chip: String? = null,
) {
    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        chip?.let { ResultChip(it, Modifier.padding(top = 3.dp)) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(223.dp)
                    .padding(horizontal = 16.dp),
            )
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp, top = 16.dp, end = 46.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ContentResult(
    result: SearchResultUiState.Content,
    onLoadNextPage: () -> Unit,
    onVacancyClicked: (String) -> Unit,
    modifier: Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadNextPage by remember(result.items.size) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            result.items.isNotEmpty() && lastVisibleIndex >= result.items.lastIndex - 2
        }
    }
    LaunchedEffect(listState, result.items.size) {
        snapshotFlow { shouldLoadNextPage }
            .distinctUntilChanged()
            .filter { shouldLoad -> shouldLoad }
            .collect { onLoadNextPage() }
    }


    Box(modifier = modifier.testTag(UiTestTags.SEARCH_RESULTS)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 44.dp),
        ) {
            itemsIndexed(
                items = result.items,
                key = { _, vacancy -> vacancy.id },
            ) { _, vacancy ->
                VacancyRow(
                    vacancy = vacancy,
                    onClick = { onVacancyClicked(vacancy.id) },
                )
            }
            if (result.isLoadingNextPage) {
                item(key = "paging_loader") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                        )
                    }
                }
            }
        }
        ResultChip(
            text = pluralStringResource(
                R.plurals.search_found_vacancies,
                result.found,
                result.found,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 3.dp),
        )
    }
}

@Composable
private fun ResultChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
    )
}

@Composable
private fun SearchField(
    state: SearchUiState,
    focusRequester: FocusRequester,
    onQueryChanged: (String) -> Unit,
    onSearchActionClicked: () -> Unit,
    onSearchSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionIcon = if (state.action == SearchAction.SEARCH) R.drawable.ic_search else R.drawable.ic_close
    val actionDescription = if (state.action == SearchAction.SEARCH) {
        R.string.search_action_description
    } else {
        R.string.clear_search_description
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
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.search_icon)),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmitted() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (state.query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = colorResource(R.color.search_hint),
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
            onSearchSubmitted = {},
            onFilterClicked = {},
            onLoadNextPage = {},
        )
    }
}

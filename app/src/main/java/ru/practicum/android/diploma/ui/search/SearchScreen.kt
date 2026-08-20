package ru.practicum.android.diploma.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.search.SearchResultUiState
import ru.practicum.android.diploma.presentation.search.SearchUiState
import ru.practicum.android.diploma.ui.components.LogoCachePolicy
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.UiTestTags
import ru.practicum.android.diploma.ui.components.VacancyRow
import ru.practicum.android.diploma.ui.theme.FigmaSearchHint
import ru.practicum.android.diploma.ui.theme.FigmaSecondary
import ru.practicum.android.diploma.ui.theme.FigmaSurfaceLight
import ru.practicum.android.diploma.ui.theme.SearchFieldContent

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearchSubmitted: () -> Unit,
    onFilterClicked: () -> Unit,
    onLoadNextPage: () -> Unit,
    onVacancyClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(UiTestTags.SEARCH_SCREEN),
    ) {
        SearchHeader(onFilterClicked)
        SearchField(
            query = state.query,
            onQueryChanged = onQueryChanged,
            onSearchSubmitted = onSearchSubmitted,
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
private fun SearchHeader(onFilterClicked: () -> Unit) {
    ScreenHeader(
        title = stringResource(R.string.search_title),
        actions = {
            IconButton(onClick = onFilterClicked, modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = stringResource(R.string.search_filter_description),
                    tint = MaterialTheme.colorScheme.onBackground,
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
    modifier: Modifier,
) {
    when (result) {
        SearchResultUiState.Initial -> IllustrationResult(
            image = R.drawable.search_initial_illustration,
            testTag = UiTestTags.SEARCH_INITIAL,
            modifier = modifier,
        )
        SearchResultUiState.Loading -> LoadingResult(modifier)
        is SearchResultUiState.Content -> ContentResult(
            result = result,
            onLoadNextPage = onLoadNextPage,
            onVacancyClicked = onVacancyClicked,
            modifier = modifier,
        )
        SearchResultUiState.Empty -> PlaceholderResult(
            image = R.drawable.search_empty_results,
            text = stringResource(R.string.search_empty_message),
            testTag = UiTestTags.SEARCH_EMPTY,
            modifier = modifier,
        )
        SearchResultUiState.NoInternet -> PlaceholderResult(
            image = R.drawable.search_no_internet,
            text = stringResource(R.string.search_no_internet_message),
            testTag = UiTestTags.SEARCH_NO_INTERNET,
            modifier = modifier,
        )
        SearchResultUiState.GenericError -> PlaceholderResult(
            image = R.drawable.search_server_error,
            text = stringResource(R.string.search_generic_error_message),
            testTag = UiTestTags.SEARCH_ERROR,
            modifier = modifier,
        )
    }
}

@Composable
private fun IllustrationResult(image: Int, testTag: String, modifier: Modifier) {
    Box(modifier = modifier.testTag(testTag), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(223.dp)
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun LoadingResult(modifier: Modifier) {
    Box(
        modifier = modifier.testTag(UiTestTags.SEARCH_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PlaceholderResult(image: Int, text: String, testTag: String, modifier: Modifier) {
    Column(
        modifier = modifier.testTag(testTag),
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
            modifier = Modifier.padding(start = 46.dp, top = 16.dp, end = 46.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
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
    val reachedLastVacancy by remember(result.items.size, result.lastLoadedPage) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            result.items.isNotEmpty() && lastVisibleIndex >= result.items.lastIndex
        }
    }
    LaunchedEffect(listState, result.items.size, result.lastLoadedPage) {
        snapshotFlow { reachedLastVacancy }
            .distinctUntilChanged()
            .filter { reachedEnd -> reachedEnd }
            .collect { onLoadNextPage() }
    }

    Box(modifier = modifier.testTag(UiTestTags.SEARCH_RESULTS)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 44.dp),
        ) {
            items(items = result.items, key = { vacancy -> vacancy.id }) { vacancy ->
                VacancyRow(
                    vacancy = vacancy,
                    missingSalaryText = stringResource(R.string.search_salary_not_specified),
                    onClick = { onVacancyClicked(vacancy.id) },
                    logoCachePolicy = LogoCachePolicy.Default,
                    logoContentDescription = stringResource(R.string.search_company_logo_description),
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val actionIcon = if (query.isEmpty()) R.drawable.ic_search else R.drawable.ic_close
    val actionDescription = if (query.isEmpty()) {
        R.string.search_action_description
    } else {
        R.string.search_clear_description
    }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSystemInDarkTheme()) FigmaSecondary else FigmaSurfaceLight)
            .padding(start = 16.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag(UiTestTags.SEARCH_INPUT),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = SearchFieldContent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearchSubmitted()
                },
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = FigmaSearchHint,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    innerTextField()
                }
            },
        )
        IconButton(
            onClick = {
                if (query.isEmpty()) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } else {
                    onQueryChanged("")
                }
            },
            modifier = Modifier
                .size(48.dp)
                .testTag(UiTestTags.SEARCH_ACTION),
        ) {
            Icon(
                painter = painterResource(actionIcon),
                contentDescription = stringResource(actionDescription),
                tint = SearchFieldContent,
            )
        }
    }
}

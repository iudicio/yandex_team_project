package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.FilterSettingsInteractor
import ru.practicum.android.diploma.domain.search.SearchError
import ru.practicum.android.diploma.domain.search.SearchOutcome
import ru.practicum.android.diploma.domain.search.SearchVacanciesInteractor
import ru.practicum.android.diploma.domain.search.VacancySearchPage
import ru.practicum.android.diploma.domain.search.VacancySearchRequest

@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught")
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val searchInteractor: SearchVacanciesInteractor,
    private val filterSettingsInteractor: FilterSettingsInteractor,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) : ViewModel() {

    private var currentFilters: FilterSettings = filterSettingsInteractor.current()
    private val _state = MutableStateFlow(
        SearchUiState(
            query = savedStateHandle.get<String>(QUERY_KEY).orEmpty(),
            hasActiveFilters = currentFilters.hasActiveFilters,
        ),
    )
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val eventChannel = Channel<SearchEvent>(capacity = Channel.BUFFERED)
    val events: Flow<SearchEvent> = eventChannel.receiveAsFlow()

    private var searchJob: Job? = null
    private var pagingJob: Job? = null
    private var generation = 0L
    private var activeRequest: VacancySearchRequest? = null
    private var currentPage = 0
    private var totalPages = 0
    private val loadedIds = mutableSetOf<String>()
    private val successfulPages = mutableSetOf<Int>()

    init {
        require(debounceMillis >= 0L) { "Debounce must not be negative" }
        viewModelScope.launch {
            filterSettingsInteractor.observe().collectLatest { settings ->
                currentFilters = settings
                _state.value = _state.value.copy(hasActiveFilters = settings.hasActiveFilters)
            }
        }
        if (_state.value.query.isNotBlank()) restartSearch(debounceMillis)
    }

    fun onQueryChanged(query: String) {
        if (query == _state.value.query) return
        savedStateHandle[QUERY_KEY] = query
        _state.value = SearchUiState(
            query = query,
            hasActiveFilters = currentFilters.hasActiveFilters,
        )
        restartSearch(debounceMillis)
    }

    fun submit() = restartSearch(delayMillis = 0L)

    fun onFiltersApplied() {
        currentFilters = filterSettingsInteractor.current()
        filterSettingsInteractor.markApplied(currentFilters)
        _state.value = _state.value.copy(hasActiveFilters = currentFilters.hasActiveFilters)
        if (_state.value.query.isNotBlank()) restartSearch(delayMillis = 0L)
    }

    fun loadNextPage() {
        val content = _state.value.result as? SearchResultUiState.Content ?: return
        val request = createNextPageRequest(content) ?: return
        val requestGeneration = generation
        _state.value = _state.value.copy(result = content.copy(isLoadingNextPage = true))
        pagingJob = viewModelScope.launch {
            when (val outcome = safeSearch(request)) {
                is SearchOutcome.Success -> if (requestGeneration == generation) {
                    applyNextPage(request, outcome.value)
                }
                is SearchOutcome.Failure -> if (requestGeneration == generation) {
                    applyPagingFailure(content, outcome.error)
                }
            }
        }
    }

    private fun createNextPageRequest(content: SearchResultUiState.Content): VacancySearchRequest? {
        val baseRequest = activeRequest ?: return null
        val nextPage = currentPage + 1
        val cannotLoad = content.isLoadingNextPage || pagingJob?.isActive == true ||
            currentPage >= totalPages || nextPage in successfulPages
        return if (cannotLoad) null else baseRequest.copy(page = nextPage)
    }

    private fun restartSearch(delayMillis: Long) {
        generation += 1
        val requestGeneration = generation
        searchJob?.cancel()
        pagingJob?.cancel()
        resetPagination()
        val query = _state.value.query
        if (query.isBlank()) {
            _state.value = SearchUiState(
                query = query,
                hasActiveFilters = currentFilters.hasActiveFilters,
            )
            return
        }
        val request = currentFilters.toSearchRequest(query)
        filterSettingsInteractor.markApplied(currentFilters)
        if (delayMillis == 0L) showLoading()
        searchJob = viewModelScope.launch {
            if (delayMillis > 0L) delay(delayMillis)
            if (requestGeneration == generation) {
                if (delayMillis > 0L) showLoading()
                applyFirstPageOutcome(request, requestGeneration, safeSearch(request))
            }
        }
    }

    private fun applyFirstPageOutcome(
        request: VacancySearchRequest,
        requestGeneration: Long,
        outcome: SearchOutcome<VacancySearchPage>,
    ) {
        if (requestGeneration != generation) return
        when (outcome) {
            is SearchOutcome.Success -> applyFirstPage(request, outcome.value)
            is SearchOutcome.Failure -> applyFirstPageFailure(outcome.error)
        }
    }

    private fun applyFirstPage(request: VacancySearchRequest, page: VacancySearchPage) {
        successfulPages += request.page
        val uniqueItems = page.items.filter { vacancy -> loadedIds.add(vacancy.id) }
        activeRequest = request
        currentPage = request.page
        totalPages = page.pages
        _state.value = _state.value.copy(
            result = if (uniqueItems.isEmpty()) {
                SearchResultUiState.Empty
            } else {
                SearchResultUiState.Content(
                    found = page.found,
                    items = uniqueItems,
                    lastLoadedPage = request.page,
                )
            },
        )
    }

    private fun applyFirstPageFailure(error: SearchError) {
        _state.value = _state.value.copy(
            result = when (error) {
                SearchError.NoInternet -> SearchResultUiState.NoInternet
                SearchError.Generic -> SearchResultUiState.GenericError
            },
        )
    }

    private fun applyNextPage(request: VacancySearchRequest, page: VacancySearchPage) {
        val content = _state.value.result as? SearchResultUiState.Content ?: return
        successfulPages += request.page
        val uniqueItems = page.items.filter { vacancy -> loadedIds.add(vacancy.id) }
        currentPage = request.page
        totalPages = page.pages
        _state.value = _state.value.copy(
            result = content.copy(
                found = page.found,
                items = content.items + uniqueItems,
                lastLoadedPage = request.page,
                isLoadingNextPage = false,
            ),
        )
    }

    private fun applyPagingFailure(content: SearchResultUiState.Content, error: SearchError) {
        _state.value = _state.value.copy(result = content.copy(isLoadingNextPage = false))
        eventChannel.trySend(SearchEvent.PagingFailed(error))
    }

    private fun showLoading() {
        _state.value = _state.value.copy(result = SearchResultUiState.Loading)
    }

    private fun resetPagination() {
        activeRequest = null
        currentPage = 0
        totalPages = 0
        loadedIds.clear()
        successfulPages.clear()
    }

    private suspend fun safeSearch(request: VacancySearchRequest): SearchOutcome<VacancySearchPage> = try {
        searchInteractor.search(request)
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        SearchOutcome.Failure(SearchError.Generic)
    }

    companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 2_000L
        const val QUERY_KEY = "search_query"
    }
}

private fun FilterSettings.toSearchRequest(query: String): VacancySearchRequest = VacancySearchRequest(
    text = query.trim(),
    salary = salary,
    onlyWithSalary = onlyWithSalary,
    industryId = industryId,
    areaId = areaId,
)

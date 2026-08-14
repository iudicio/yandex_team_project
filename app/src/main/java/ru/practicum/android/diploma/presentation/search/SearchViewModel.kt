package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.domain.search.VacancyRepository
import ru.practicum.android.diploma.domain.search.VacancySearchRequest
import ru.practicum.android.diploma.domain.search.VacancySearchResult
import ru.practicum.android.diploma.domain.search.toVacancySearchRequest
import java.io.IOException

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val vacancyRepository: VacancyRepository,
    private val filterSettingsRepository: FilterSettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SearchUiState(query = savedStateHandle.get<String>(QUERY_KEY).orEmpty()),
    )
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SearchEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SearchEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null
    private var pagingJob: Job? = null
    private var generation = 0L
    private var activeRequest: VacancySearchRequest? = null
    private var currentPage = 0
    private var totalPages = 0
    private val loadedIds = mutableSetOf<String>()

    init {
        require(debounceMillis >= 0L) { "Debounce must not be negative" }
        if (_state.value.query.isNotBlank()) {
            restartSearch(delayMillis = debounceMillis)
        }
    }

    fun onQueryChanged(query: String) {
        if (query == _state.value.query) return

        savedStateHandle[QUERY_KEY] = query
        _state.value = SearchUiState(query = query)
        restartSearch(delayMillis = debounceMillis)
    }

    fun submit() {
        restartSearch(delayMillis = 0L)
    }

    fun onFiltersApplied() {
        restartSearch(delayMillis = 0L)
    }

    fun loadNextPage() {
        val content = _state.value.result as? SearchResultUiState.Content ?: return
        val request = nextPageRequest(content) ?: return
        val requestGeneration = generation
        _state.value = _state.value.copy(result = content.copy(isLoadingNextPage = true))
        pagingJob = viewModelScope.launch(dispatcher) {
            val result = safeSearch(request)
            if (requestGeneration == generation) {
                result.fold(
                    onSuccess = { response -> applyNextPage(request, response) },
                    onFailure = { throwable -> applyPagingFailure(content, throwable) },
                )
            }
        }
    }

    private fun nextPageRequest(content: SearchResultUiState.Content): VacancySearchRequest? {
        val baseRequest = activeRequest ?: return null
        val cannotLoad = content.isLoadingNextPage || currentPage >= totalPages || pagingJob?.isActive == true
        return if (cannotLoad) null else baseRequest.copy(page = currentPage + 1)
    }

    private fun restartSearch(delayMillis: Long) {
        generation += 1
        val requestGeneration = generation
        cancelRunningRequests()
        resetPagination()

        val query = _state.value.query
        if (query.isBlank()) {
            _state.value = SearchUiState(query = query)
            return
        }

        val request = filterSettingsRepository.load().toVacancySearchRequest(
            text = query,
            page = FIRST_PAGE,
        )
        if (delayMillis == 0L) {
            showLoading()
        }
        searchJob = viewModelScope.launch(dispatcher) {
            if (delayMillis > 0L) {
                delay(delayMillis)
            }
            if (requestGeneration == generation) {
                if (delayMillis > 0L) showLoading()
                executeFirstPage(request, requestGeneration)
            }
        }
    }

    private suspend fun executeFirstPage(request: VacancySearchRequest, requestGeneration: Long) {
        val result = safeSearch(request)
        if (requestGeneration != generation) return

        result.fold(
            onSuccess = { response -> applyFirstPage(request, response) },
            onFailure = { throwable -> applyFirstPageFailure(throwable) },
        )
    }

    private fun applyFirstPage(request: VacancySearchRequest, response: VacancySearchResult) {
        loadedIds.clear()
        val uniqueItems = response.items.filter { vacancy -> loadedIds.add(vacancy.id) }
        activeRequest = request
        currentPage = request.page
        totalPages = response.pages
        _state.value = _state.value.copy(
            result = if (uniqueItems.isEmpty()) {
                SearchResultUiState.Empty
            } else {
                SearchResultUiState.Content(
                    found = response.found,
                    items = uniqueItems,
                )
            },
        )
    }

    private fun applyFirstPageFailure(throwable: Throwable) {
        _state.value = _state.value.copy(
            result = when (throwable) {
                is CancellationException -> throw throwable
                is IOException -> SearchResultUiState.NoInternet(throwable)
                is Exception -> SearchResultUiState.Error(throwable)
                else -> throw throwable
            },
        )
    }

    private fun applyNextPage(request: VacancySearchRequest, response: VacancySearchResult) {
        val content = _state.value.result as? SearchResultUiState.Content ?: return
        val uniqueItems = response.items.filter { vacancy -> loadedIds.add(vacancy.id) }
        currentPage = request.page
        totalPages = response.pages
        _state.value = _state.value.copy(
            result = content.copy(
                found = response.found,
                items = content.items + uniqueItems,
                isLoadingNextPage = false,
            ),
        )
    }

    private fun applyPagingFailure(content: SearchResultUiState.Content, throwable: Throwable) {
        val exception = when (throwable) {
            is CancellationException -> throw throwable
            is Exception -> throwable
            else -> throw throwable
        }
        _state.value = _state.value.copy(result = content.copy(isLoadingNextPage = false))
        _events.tryEmit(SearchEvent.PagingFailed(exception))
    }

    private fun showLoading() {
        _state.value = _state.value.copy(result = SearchResultUiState.Loading)
    }

    private fun cancelRunningRequests() {
        searchJob?.cancel()
        pagingJob?.cancel()
        searchJob = null
        pagingJob = null
    }

    private fun resetPagination() {
        activeRequest = null
        currentPage = 0
        totalPages = 0
        loadedIds.clear()
    }

    @Suppress("detekt.TooGenericExceptionCaught")
    private suspend fun safeSearch(request: VacancySearchRequest): Result<VacancySearchResult> = try {
        vacancyRepository.search(request)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    companion object {
        const val QUERY_KEY = "search_query"
        const val DEFAULT_DEBOUNCE_MILLIS = 2_000L
        private const val FIRST_PAGE = 1
    }
}

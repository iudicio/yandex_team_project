package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.CatalogRepository

class IndustryViewModel(
    private val repository: CatalogRepository,
    selectedIndustryId: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(IndustryUiState(selectedIndustryId = selectedIndustryId))
    val state: StateFlow<IndustryUiState> = _state.asStateFlow()

    init {
        loadIndustries()
    }

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun onIndustrySelected(industryId: String) {
        _state.value = _state.value.copy(selectedIndustryId = industryId)
    }

    fun retry() {
        loadIndustries()
    }

    private fun loadIndustries() {
        _state.value = _state.value.copy(loadState = CatalogLoadState.LOADING)
        viewModelScope.launch {
            repository.getIndustries().fold(
                onSuccess = { industries ->
                    val selectedId = _state.value.selectedIndustryId
                        ?.takeIf { id -> industries.any { industry -> industry.id == id } }
                    _state.value = _state.value.copy(
                        loadState = CatalogLoadState.CONTENT,
                        industries = industries,
                        selectedIndustryId = selectedId,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(loadState = CatalogLoadState.ERROR)
                },
            )
        }
    }
}

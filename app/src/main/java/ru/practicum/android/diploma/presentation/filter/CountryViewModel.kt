package ru.practicum.android.diploma.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filter.CatalogRepository

class CountryViewModel(
    private val repository: CatalogRepository,
    selectedCountryId: Int?,
) : ViewModel() {

    private val _state = MutableStateFlow(CountryUiState(selectedCountryId = selectedCountryId))
    val state: StateFlow<CountryUiState> = _state.asStateFlow()

    init {
        loadCountries()
    }

    fun retry() {
        loadCountries()
    }

    private fun loadCountries() {
        _state.value = _state.value.copy(loadState = CatalogLoadState.LOADING)
        viewModelScope.launch {
            repository.getCountries().fold(
                onSuccess = { countries ->
                    _state.value = _state.value.copy(
                        loadState = CatalogLoadState.CONTENT,
                        countries = countries,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(loadState = CatalogLoadState.ERROR)
                },
            )
        }
    }
}

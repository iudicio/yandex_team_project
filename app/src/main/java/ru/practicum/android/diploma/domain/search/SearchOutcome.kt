package ru.practicum.android.diploma.domain.search

sealed interface SearchError {
    data object NoInternet : SearchError
    data object Generic : SearchError
}

sealed interface SearchOutcome<out T> {
    data class Success<T>(val value: T) : SearchOutcome<T>
    data class Failure(val error: SearchError) : SearchOutcome<Nothing>
}

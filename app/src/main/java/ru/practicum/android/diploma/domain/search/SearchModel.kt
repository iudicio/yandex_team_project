package ru.practicum.android.diploma.domain.search

interface SearchModel {
    var query: String
}

class InMemorySearchModel(
    override var query: String = "",
) : SearchModel

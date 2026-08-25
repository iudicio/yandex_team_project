package ru.practicum.android.diploma.domain.filter

interface IndustriesInteractor {
    suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>>
}

class IndustriesInteractorImpl(
    private val repository: IndustriesRepository,
) : IndustriesInteractor {
    override suspend fun loadIndustries(): FilterCatalogOutcome<List<Industry>> = repository.loadIndustries()
}

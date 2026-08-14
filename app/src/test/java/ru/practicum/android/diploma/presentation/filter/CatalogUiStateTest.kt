package ru.practicum.android.diploma.presentation.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.RegionSelection

class CatalogUiStateTest {

    @Test
    fun `region search trims query ignores case and matches substring`() {
        val state = RegionUiState(
            loadState = CatalogLoadState.CONTENT,
            query = "  СКВа  ",
            regions = REGIONS,
        )

        assertEquals(listOf(MOSCOW), state.visibleRegions)
    }

    @Test
    fun `blank region query leaves loaded order unchanged`() {
        val state = RegionUiState(
            loadState = CatalogLoadState.CONTENT,
            query = "  ",
            regions = REGIONS,
        )

        assertEquals(REGIONS, state.visibleRegions)
    }

    @Test
    fun `region query without matches exposes empty local result`() {
        val state = RegionUiState(
            loadState = CatalogLoadState.CONTENT,
            query = "несуществующий",
            regions = REGIONS,
        )

        assertTrue(state.visibleRegions.isEmpty())
        assertEquals(CatalogLoadState.CONTENT, state.loadState)
    }

    @Test
    fun `industry search ignores case and selected item is resolved by id`() {
        val state = IndustryUiState(
            loadState = CatalogLoadState.CONTENT,
            query = "  разработка  ",
            industries = INDUSTRIES,
            selectedIndustryId = DEVELOPMENT.id,
        )

        assertEquals(listOf(DEVELOPMENT), state.visibleIndustries)
        assertEquals(DEVELOPMENT, state.selectedIndustry)
    }

    @Test
    fun `unknown selected industry resolves to null`() {
        val state = IndustryUiState(
            industries = INDUSTRIES,
            selectedIndustryId = "missing",
        )

        assertNull(state.selectedIndustry)
    }

    @Test
    fun `country state keeps selected country id from current settings`() {
        val state = CountryUiState(
            loadState = CatalogLoadState.CONTENT,
            countries = listOf(RUSSIA),
            selectedCountryId = RUSSIA.id,
        )

        assertEquals(RUSSIA.id, state.selectedCountryId)
    }

    @Test
    fun `local region search does not discard selected region id`() {
        val state = RegionUiState(
            loadState = CatalogLoadState.CONTENT,
            query = "Туль",
            regions = REGIONS,
            selectedRegionId = MOSCOW.id,
        )

        assertEquals(listOf(TULA), state.visibleRegions)
        assertEquals(MOSCOW.id, state.selectedRegionId)
    }

    private companion object {
        val RUSSIA = AreaSelection(1, "Россия")
        val MOSCOW = RegionSelection(
            id = 10,
            name = "Москва",
            countryId = 1,
            countryName = "Россия",
        )
        val TULA = RegionSelection(
            id = 11,
            name = "Тульская область",
            countryId = 1,
            countryName = "Россия",
        )
        val REGIONS = listOf(MOSCOW, TULA)
        val DEVELOPMENT = Industry("7", "Разработка программного обеспечения")
        val CONSTRUCTION = Industry("8", "Строительство")
        val INDUSTRIES = listOf(DEVELOPMENT, CONSTRUCTION)
    }
}

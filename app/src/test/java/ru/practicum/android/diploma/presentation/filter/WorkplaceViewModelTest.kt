package ru.practicum.android.diploma.presentation.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.FilterSettings
import ru.practicum.android.diploma.domain.filter.RegionSelection

class WorkplaceViewModelTest {

    @Test
    fun `initial settings are restored and enable choose`() {
        val viewModel = WorkplaceViewModel(
            FilterSettings(
                countryId = RUSSIA.id,
                countryName = RUSSIA.name,
                regionId = MOSCOW.id,
                regionName = MOSCOW.name,
            ),
        )

        assertEquals(RUSSIA.id, viewModel.state.value.countryId)
        assertEquals(MOSCOW.id, viewModel.state.value.regionId)
        assertTrue(viewModel.state.value.canChoose)
    }

    @Test
    fun `selecting another country clears incompatible region`() {
        val viewModel = WorkplaceViewModel(RUSSIA_AND_MOSCOW)

        viewModel.onCountrySelected(BELARUS)

        assertEquals(BELARUS.id, viewModel.state.value.countryId)
        assertEquals(BELARUS.name, viewModel.state.value.countryName)
        assertNull(viewModel.state.value.regionId)
        assertNull(viewModel.state.value.regionName)
        assertTrue(viewModel.state.value.canChoose)
    }

    @Test
    fun `selecting same country keeps selected region`() {
        val viewModel = WorkplaceViewModel(RUSSIA_AND_MOSCOW)

        viewModel.onCountrySelected(RUSSIA)

        assertEquals(MOSCOW.id, viewModel.state.value.regionId)
        assertEquals(MOSCOW.name, viewModel.state.value.regionName)
    }

    @Test
    fun `selecting region automatically selects its parent country`() {
        val viewModel = WorkplaceViewModel(FilterSettings())

        viewModel.onRegionSelected(MINSK)

        assertEquals(BELARUS.id, viewModel.state.value.countryId)
        assertEquals(BELARUS.name, viewModel.state.value.countryName)
        assertEquals(MINSK.id, viewModel.state.value.regionId)
        assertEquals(MINSK.name, viewModel.state.value.regionName)
        assertTrue(viewModel.state.value.canChoose)
    }

    @Test
    fun `clearing region keeps country while clearing country clears both`() {
        val viewModel = WorkplaceViewModel(RUSSIA_AND_MOSCOW)

        viewModel.onRegionCleared()

        assertEquals(RUSSIA.id, viewModel.state.value.countryId)
        assertNull(viewModel.state.value.regionId)
        assertTrue(viewModel.state.value.canChoose)

        viewModel.onCountryCleared()

        assertNull(viewModel.state.value.countryId)
        assertNull(viewModel.state.value.regionId)
        assertFalse(viewModel.state.value.canChoose)
    }

    private companion object {
        val RUSSIA = AreaSelection(1, "Россия")
        val BELARUS = AreaSelection(2, "Беларусь")
        val MOSCOW = RegionSelection(
            id = 10,
            name = "Москва",
            countryId = RUSSIA.id,
            countryName = RUSSIA.name,
        )
        val MINSK = RegionSelection(
            id = 20,
            name = "Минск",
            countryId = BELARUS.id,
            countryName = BELARUS.name,
        )
        val RUSSIA_AND_MOSCOW = FilterSettings(
            countryId = RUSSIA.id,
            countryName = RUSSIA.name,
            regionId = MOSCOW.id,
            regionName = MOSCOW.name,
        )
    }
}

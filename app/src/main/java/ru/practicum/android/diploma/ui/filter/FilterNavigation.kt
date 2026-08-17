package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import ru.practicum.android.diploma.domain.filter.AreaSelection
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.filter.RegionSelection
import ru.practicum.android.diploma.presentation.filter.WorkplaceUiState

internal const val COUNTRY_RESULT_KEY = "filter_country_result"
internal const val REGION_RESULT_KEY = "filter_region_result"
internal const val WORKPLACE_RESULT_KEY = "filter_workplace_result"
internal const val INDUSTRY_RESULT_KEY = "filter_industry_result"

internal const val ARG_COUNTRY_ID = "countryId"
internal const val ARG_REGION_ID = "regionId"
internal const val ARG_INDUSTRY_ID = "industryId"

internal fun NavController.navigateFrom(
    @IdRes expectedDestination: Int,
    @IdRes action: Int,
    arguments: Bundle? = null,
) {
    if (currentDestination?.id == expectedDestination) {
        navigate(action, arguments)
    }
}

internal inline fun NavController.returnResultFrom(
    @IdRes expectedDestination: Int,
    setResult: (androidx.lifecycle.SavedStateHandle) -> Unit,
) {
    if (currentDestination?.id == expectedDestination) {
        previousBackStackEntry?.savedStateHandle?.let(setResult)
        popBackStack()
    }
}

internal fun NavController.popFrom(@IdRes expectedDestination: Int) {
    if (currentDestination?.id == expectedDestination) {
        popBackStack()
    }
}

private const val KEY_ID = "id"
private const val KEY_NAME = "name"
private const val KEY_COUNTRY_NAME = "countryName"
private const val KEY_REGION_NAME = "regionName"

internal fun AreaSelection.toResultBundle(): Bundle = Bundle().apply {
    putInt(KEY_ID, id)
    putString(KEY_NAME, name)
}

internal fun Bundle.toAreaSelection(): AreaSelection = AreaSelection(
    id = getInt(KEY_ID),
    name = getString(KEY_NAME).orEmpty(),
)

internal fun RegionSelection.toResultBundle(): Bundle = Bundle().apply {
    putInt(KEY_ID, id)
    putString(KEY_NAME, name)
    putInt(ARG_COUNTRY_ID, countryId)
    putString(KEY_COUNTRY_NAME, countryName)
}

internal fun Bundle.toRegionSelection(): RegionSelection = RegionSelection(
    id = getInt(KEY_ID),
    name = getString(KEY_NAME).orEmpty(),
    countryId = getInt(ARG_COUNTRY_ID),
    countryName = getString(KEY_COUNTRY_NAME).orEmpty(),
)

internal fun Industry.toResultBundle(): Bundle = Bundle().apply {
    putString(KEY_ID, id)
    putString(KEY_NAME, name)
}

internal fun Bundle.toIndustry(): Industry = Industry(
    id = getString(KEY_ID).orEmpty(),
    name = getString(KEY_NAME).orEmpty(),
)

internal fun WorkplaceUiState.toResultBundle(): Bundle = Bundle().apply {
    countryId?.let { putInt(ARG_COUNTRY_ID, it) }
    putString(KEY_COUNTRY_NAME, countryName)
    regionId?.let { putInt(ARG_REGION_ID, it) }
    putString(KEY_REGION_NAME, regionName)
}

internal fun Bundle.toWorkplaceUiState(): WorkplaceUiState = WorkplaceUiState(
    countryId = nullableInt(ARG_COUNTRY_ID),
    countryName = getString(KEY_COUNTRY_NAME),
    regionId = nullableInt(ARG_REGION_ID),
    regionName = getString(KEY_REGION_NAME),
)

internal fun Bundle.nullableInt(key: String): Int? =
    if (containsKey(key)) getInt(key).takeIf { value -> value >= 0 } else null

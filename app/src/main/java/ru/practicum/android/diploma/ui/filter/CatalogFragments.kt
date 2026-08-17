package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.filter.CountryViewModel
import ru.practicum.android.diploma.presentation.filter.IndustryViewModel
import ru.practicum.android.diploma.presentation.filter.RegionViewModel
import ru.practicum.android.diploma.presentation.filter.WorkplaceViewModel
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class WorkplaceFragment : Fragment() {

    private val viewModel: WorkplaceViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.workplace_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                WorkplaceScreen(
                    state = state,
                    onBackClicked = { findNavController().popFrom(R.id.navigationWorkplace) },
                    onCountryClicked = ::openCountry,
                    onCountryClearClicked = viewModel::onCountryCleared,
                    onRegionClicked = ::openRegion,
                    onRegionClearClicked = viewModel::onRegionCleared,
                    onChooseClicked = ::chooseWorkplace,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle.getLiveData<Bundle>(COUNTRY_RESULT_KEY).observe(viewLifecycleOwner) { result ->
            viewModel.onCountrySelected(result.toAreaSelection())
            handle.remove<Bundle>(COUNTRY_RESULT_KEY)
        }
        handle.getLiveData<Bundle>(REGION_RESULT_KEY).observe(viewLifecycleOwner) { result ->
            viewModel.onRegionSelected(result.toRegionSelection())
            handle.remove<Bundle>(REGION_RESULT_KEY)
        }
    }

    private fun openCountry() {
        val args = Bundle().apply {
            viewModel.state.value.countryId?.let { putInt(ARG_COUNTRY_ID, it) }
        }
        findNavController().navigateFrom(
            expectedDestination = R.id.navigationWorkplace,
            action = R.id.action_navigationWorkplace_to_navigationCountry,
            arguments = args,
        )
    }

    private fun openRegion() {
        val state = viewModel.state.value
        val args = Bundle().apply {
            state.countryId?.let { putInt(ARG_COUNTRY_ID, it) }
            state.regionId?.let { putInt(ARG_REGION_ID, it) }
        }
        findNavController().navigateFrom(
            expectedDestination = R.id.navigationWorkplace,
            action = R.id.action_navigationWorkplace_to_navigationRegion,
            arguments = args,
        )
    }

    private fun chooseWorkplace() {
        findNavController().returnResultFrom(R.id.navigationWorkplace) { handle ->
            handle[WORKPLACE_RESULT_KEY] = viewModel.state.value.toResultBundle()
        }
    }
}

class CountryFragment : Fragment() {

    private val viewModel: CountryViewModel by viewModel {
        parametersOf(arguments?.nullableInt(ARG_COUNTRY_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.country_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                CountryScreen(
                    state = state,
                    onBackClicked = { findNavController().popFrom(R.id.navigationCountry) },
                    onCountryClicked = { country ->
                        findNavController().returnResultFrom(R.id.navigationCountry) { handle ->
                            handle[COUNTRY_RESULT_KEY] = country.toResultBundle()
                        }
                    },
                    onRetryClicked = viewModel::retry,
                )
            }
        }
    }
}

class RegionFragment : Fragment() {

    private val viewModel: RegionViewModel by viewModel {
        parametersOf(
            arguments?.nullableInt(ARG_COUNTRY_ID),
            arguments?.nullableInt(ARG_REGION_ID)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.region_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                RegionScreen(
                    state = state,
                    onBackClicked = { findNavController().popFrom(R.id.navigationRegion) },
                    onQueryChanged = viewModel::onQueryChanged,
                    onRegionClicked = { region ->
                        findNavController().returnResultFrom(R.id.navigationRegion) { handle ->
                            handle[REGION_RESULT_KEY] = region.toResultBundle()
                        }
                    },
                    onRetryClicked = viewModel::retry,
                )
            }
        }
    }
}

class IndustryFragment : Fragment() {

    private val viewModel: IndustryViewModel by viewModel {
        parametersOf(arguments?.getString(ARG_INDUSTRY_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.industry_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                IndustryScreen(
                    state = state,
                    onBackClicked = { findNavController().popFrom(R.id.navigationIndustry) },
                    onQueryChanged = viewModel::onQueryChanged,
                    onIndustryClicked = { industry -> viewModel.onIndustrySelected(industry.id) },
                    onChooseClicked = { industry ->
                        findNavController().returnResultFrom(R.id.navigationIndustry) { handle ->
                            handle[INDUSTRY_RESULT_KEY] = industry.toResultBundle()
                        }
                    },
                    onRetryClicked = viewModel::retry,
                )
            }
        }
    }
}

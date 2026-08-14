package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.di.appContainer
import ru.practicum.android.diploma.domain.filter.CatalogRepository
import ru.practicum.android.diploma.domain.filter.FilterSettingsRepository
import ru.practicum.android.diploma.presentation.filter.CountryViewModel
import ru.practicum.android.diploma.presentation.filter.IndustryViewModel
import ru.practicum.android.diploma.presentation.filter.RegionViewModel
import ru.practicum.android.diploma.presentation.filter.WorkplaceViewModel
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class WorkplaceFragment : Fragment() {

    private val viewModel: WorkplaceViewModel by viewModels {
        WorkplaceViewModelFactory(requireContext().appContainer.filterSettingsRepository)
    }

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

    private val viewModel: CountryViewModel by viewModels {
        CountryViewModelFactory(
            repository = requireContext().appContainer.catalogRepository,
            selectedCountryId = arguments?.nullableInt(ARG_COUNTRY_ID),
        )
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

    private val viewModel: RegionViewModel by viewModels {
        RegionViewModelFactory(
            repository = requireContext().appContainer.catalogRepository,
            countryId = arguments?.nullableInt(ARG_COUNTRY_ID),
            selectedRegionId = arguments?.nullableInt(ARG_REGION_ID),
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

    private val viewModel: IndustryViewModel by viewModels {
        IndustryViewModelFactory(
            repository = requireContext().appContainer.catalogRepository,
            selectedIndustryId = arguments
                ?.getString(ARG_INDUSTRY_ID)
                ?.takeIf(String::isNotEmpty),
        )
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

private class WorkplaceViewModelFactory(
    private val repository: FilterSettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        requireNotNull(modelClass.cast(WorkplaceViewModel(repository.load())))
}

private class CountryViewModelFactory(
    private val repository: CatalogRepository,
    private val selectedCountryId: Int?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        requireNotNull(modelClass.cast(CountryViewModel(repository, selectedCountryId)))
}

private class RegionViewModelFactory(
    private val repository: CatalogRepository,
    private val countryId: Int?,
    private val selectedRegionId: Int?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        requireNotNull(modelClass.cast(RegionViewModel(repository, countryId, selectedRegionId)))
}

private class IndustryViewModelFactory(
    private val repository: CatalogRepository,
    private val selectedIndustryId: String?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        requireNotNull(modelClass.cast(IndustryViewModel(repository, selectedIndustryId)))
}

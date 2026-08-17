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
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.filter.FilterViewModel
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class FilterFragment : Fragment() {

    private val viewModel: FilterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.filter_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                FilterScreen(
                    state = state,
                    onBackClicked = ::navigateBack,
                    onResetClicked = viewModel::onResetClicked,
                    onWorkplaceClicked = ::openWorkplace,
                    onWorkplaceClearClicked = viewModel::onWorkplaceCleared,
                    onIndustryClicked = ::openIndustry,
                    onIndustryClearClicked = viewModel::onIndustryCleared,
                    onSalaryChanged = viewModel::onSalaryChanged,
                    onSalaryClearClicked = { viewModel.onSalaryChanged("") },
                    onOnlyWithSalaryChanged = viewModel::onOnlyWithSalaryChanged,
                    onApplyClicked = ::applyFilters,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle.getLiveData<Bundle>(WORKPLACE_RESULT_KEY).observe(viewLifecycleOwner) { result ->
            val workplace = result.toWorkplaceUiState()
            viewModel.onWorkplaceSelected(
                countryId = workplace.countryId,
                countryName = workplace.countryName,
                regionId = workplace.regionId,
                regionName = workplace.regionName,
            )
            handle.remove<Bundle>(WORKPLACE_RESULT_KEY)
        }
        handle.getLiveData<Bundle>(INDUSTRY_RESULT_KEY).observe(viewLifecycleOwner) { result ->
            val industry = result.toIndustry()
            viewModel.onIndustrySelected(industry.id, industry.name)
            handle.remove<Bundle>(INDUSTRY_RESULT_KEY)
        }
    }

    private fun navigateBack() {
        findNavController().popFrom(R.id.navigationFilter)
    }

    private fun applyFilters() {
        findNavController().returnResultFrom(R.id.navigationFilter) { handle ->
            handle[FILTERS_APPLIED_RESULT_KEY] = true
        }
    }

    private fun openWorkplace() {
        findNavController().navigateFrom(
            expectedDestination = R.id.navigationFilter,
            action = R.id.action_navigationFilter_to_navigationWorkplace,
        )
    }

    private fun openIndustry() {
        val arguments = Bundle().apply {
            viewModel.state.value.industryId?.let { putString(ARG_INDUSTRY_ID, it) }
        }
        findNavController().navigateFrom(
            expectedDestination = R.id.navigationFilter,
            action = R.id.action_navigationFilter_to_navigationIndustry,
            arguments = arguments,
        )
    }

    companion object {
        const val FILTERS_APPLIED_RESULT_KEY = "filters_applied"
    }
}

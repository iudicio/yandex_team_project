package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.filter.FilterEvent
import ru.practicum.android.diploma.presentation.filter.FilterViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.FILTER_APPLIED_RESULT_KEY

class FilterFragment : ComposeDestinationFragment() {

    private val viewModel: FilterViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        FilterScreen(
            state = state.filterScreenModel(),
            onBack = viewModel::back,
            onReset = viewModel::reset,
            onWorkplaceClick = {
                if (navController.currentDestination?.id == R.id.filterFragment) {
                    navController.navigate(R.id.action_filter_to_workplace)
                }
            },
            onWorkplaceClear = viewModel::clearWorkplace,
            onIndustryClick = {
                if (navController.currentDestination?.id == R.id.filterFragment) {
                    navController.navigate(R.id.action_filter_to_industry)
                }
            },
            onIndustryClear = viewModel::clearIndustry,
            onSalaryChanged = viewModel::onSalaryChanged,
            onOnlyWithSalaryChanged = viewModel::onOnlyWithSalaryChanged,
            onApply = viewModel::apply,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect(::handleEvent)
            }
        }
    }

    private fun handleEvent(event: FilterEvent) {
        val navController = findNavController()
        when (event) {
            is FilterEvent.Applied -> {
                navController.previousBackStackEntry?.savedStateHandle
                    ?.set(FILTER_APPLIED_RESULT_KEY, true)
                navController.popBackStack()
            }
            FilterEvent.Close -> navController.popBackStack()
        }
    }
}

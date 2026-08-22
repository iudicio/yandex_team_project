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
import ru.practicum.android.diploma.presentation.filter.WorkplaceEvent
import ru.practicum.android.diploma.presentation.filter.WorkplaceViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment

class WorkplaceFragment : ComposeDestinationFragment() {

    private val viewModel: WorkplaceViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        WorkplaceScreen(
            state = state.workplaceScreenModel(),
            onBack = viewModel::back,
            onCountryClick = {
                if (navController.currentDestination?.id == R.id.workplaceFragment) {
                    navController.navigate(R.id.action_workplace_to_country)
                }
            },
            onCountryClear = viewModel::clearCountry,
            onRegionClick = {
                if (navController.currentDestination?.id == R.id.workplaceFragment) {
                    navController.navigate(R.id.action_workplace_to_region)
                }
            },
            onRegionClear = viewModel::clearRegion,
            onConfirm = viewModel::confirm,
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

    private fun handleEvent(event: WorkplaceEvent) {
        when (event) {
            is WorkplaceEvent.Confirmed,
            WorkplaceEvent.Close,
            -> findNavController().popBackStack()
        }
    }
}

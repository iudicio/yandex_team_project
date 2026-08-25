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
import ru.practicum.android.diploma.presentation.filter.AreaResultUiState
import ru.practicum.android.diploma.presentation.filter.RegionEvent
import ru.practicum.android.diploma.presentation.filter.RegionViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment

class RegionFragment : ComposeDestinationFragment() {

    private val viewModel: RegionViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        RegionScreen(
            state = state.regionScreenModel(),
            onBack = viewModel::back,
            onQueryChanged = viewModel::onQueryChanged,
            onClearQuery = viewModel::clearQuery,
            onRetry = viewModel::retry,
            onRegionClick = ::selectRegion,
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

    private fun selectRegion(item: FilterCatalogItem) {
        val content = viewModel.state.value.result as? AreaResultUiState.Content ?: return
        content.items.firstOrNull { region -> region.id.toString() == item.id }?.let(viewModel::select)
    }

    private fun handleEvent(event: RegionEvent) {
        when (event) {
            is RegionEvent.Selected,
            RegionEvent.Close,
            -> findNavController().popBackStack()
        }
    }
}

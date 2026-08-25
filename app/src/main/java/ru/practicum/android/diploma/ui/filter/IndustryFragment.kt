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
import ru.practicum.android.diploma.presentation.filter.IndustryEvent
import ru.practicum.android.diploma.presentation.filter.IndustryResultUiState
import ru.practicum.android.diploma.presentation.filter.IndustryViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment

class IndustryFragment : ComposeDestinationFragment() {

    private val viewModel: IndustryViewModel by viewModel()

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        IndustryScreen(
            state = state.industryScreenModel(),
            onBack = viewModel::back,
            onQueryChanged = viewModel::onQueryChanged,
            onClearQuery = viewModel::clearQuery,
            onRetry = viewModel::retry,
            onIndustryClick = ::selectIndustry,
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

    private fun selectIndustry(item: FilterCatalogItem) {
        val content = viewModel.state.value.result as? IndustryResultUiState.Content ?: return
        content.items.firstOrNull { industry -> industry.id == item.id }?.let(viewModel::select)
    }

    private fun handleEvent(event: IndustryEvent) {
        when (event) {
            is IndustryEvent.Confirmed,
            IndustryEvent.Close,
            -> findNavController().popBackStack()
        }
    }
}

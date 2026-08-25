package ru.practicum.android.diploma.ui.filter

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderAction
import ru.practicum.android.diploma.ui.common.PlaceholderScreen

class FilterFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        PlaceholderScreen(
            titleRes = R.string.screen_filter,
            onBack = { navController.navigateUp() },
        ) {
            PlaceholderAction(labelRes = R.string.action_open_workplace) {
                navController.navigate(R.id.action_filter_to_workplace)
            }
            PlaceholderAction(labelRes = R.string.action_open_industry) {
                navController.navigate(R.id.action_filter_to_industry)
            }
        }
    }
}

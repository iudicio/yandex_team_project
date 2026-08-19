package ru.practicum.android.diploma.ui.filter

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderAction
import ru.practicum.android.diploma.ui.common.PlaceholderScreen

class WorkplaceFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        PlaceholderScreen(
            titleRes = R.string.screen_workplace,
            onBack = { navController.navigateUp() },
        ) {
            PlaceholderAction(labelRes = R.string.action_open_country) {
                navController.navigate(R.id.action_workplace_to_country)
            }
            PlaceholderAction(labelRes = R.string.action_open_region) {
                navController.navigate(R.id.action_workplace_to_region)
            }
        }
    }
}

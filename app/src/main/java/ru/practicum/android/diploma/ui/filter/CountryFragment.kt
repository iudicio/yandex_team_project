package ru.practicum.android.diploma.ui.filter

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderScreen

class CountryFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        PlaceholderScreen(
            titleRes = R.string.screen_country,
            onBack = { navController.navigateUp() },
        )
    }
}

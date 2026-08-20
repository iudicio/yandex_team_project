package ru.practicum.android.diploma.ui.team

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderScreen

class TeamFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        PlaceholderScreen(titleRes = R.string.screen_team)
    }
}

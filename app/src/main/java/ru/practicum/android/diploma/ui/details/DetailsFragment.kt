package ru.practicum.android.diploma.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderScreen
import ru.practicum.android.diploma.ui.common.VACANCY_ID_ARGUMENT

class DetailsFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        val vacancyId = arguments?.getString(VACANCY_ID_ARGUMENT).orEmpty()
        PlaceholderScreen(
            titleRes = R.string.screen_details,
            message = stringResource(R.string.details_vacancy_id, vacancyId),
            onBack = { navController.navigateUp() },
        )
    }
}

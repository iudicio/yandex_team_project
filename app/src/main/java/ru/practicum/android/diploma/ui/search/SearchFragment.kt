package ru.practicum.android.diploma.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.PlaceholderAction
import ru.practicum.android.diploma.ui.common.PlaceholderScreen
import ru.practicum.android.diploma.ui.common.vacancyArguments

class SearchFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        val previewVacancyId = stringResource(R.string.preview_vacancy_id)
        PlaceholderScreen(titleRes = R.string.screen_search) {
            PlaceholderAction(labelRes = R.string.action_open_filters) {
                navController.navigate(R.id.action_search_to_filter)
            }
            PlaceholderAction(labelRes = R.string.action_open_details) {
                navController.navigate(
                    R.id.action_search_to_details,
                    vacancyArguments(previewVacancyId),
                )
            }
        }
    }
}

package ru.practicum.android.diploma.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.components.ScreenHeader

internal const val TEAM_SCREEN_TAG = "team_screen"

class TeamFragment : ComposeDestinationFragment() {
    @Composable
    override fun DestinationContent(navController: NavController) {
        TeamScreen()
    }
}

@Composable
fun TeamScreen(modifier: Modifier = Modifier) {
    val members = stringArrayResource(R.array.team_members)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(TEAM_SCREEN_TAG),
    ) {
        ScreenHeader(title = stringResource(R.string.team_title))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            members.forEach { member ->
                Text(
                    text = member,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

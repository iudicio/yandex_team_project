package ru.practicum.android.diploma.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.practicum.android.diploma.domain.search.VacancyCard
import ru.practicum.android.diploma.domain.search.formatSalary

@Composable
fun VacancyRow(
    vacancy: VacancyCard,
    missingSalaryText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    logoCachePolicy: LogoCachePolicy = LogoCachePolicy.Default,
    logoContentDescription: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        VacancyLogo(
            logoUrl = vacancy.logo,
            contentDescription = logoContentDescription,
            cachePolicy = logoCachePolicy,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(vacancy.name, vacancy.city).joinToString(", "),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            vacancy.company?.let { company ->
                Text(
                    text = company,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = vacancy.salary.formatSalary(missingSalaryText),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

package ru.practicum.android.diploma.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.theme.AppDimensions

@Composable
fun PlaceholderScreen(
    @StringRes titleRes: Int,
    message: String = stringResource(R.string.placeholder_message),
    onBack: (() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.dp16),
        verticalArrangement = Arrangement.spacedBy(
            space = AppDimensions.dp16,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
        actions()
        if (onBack != null) {
            OutlinedButton(onClick = onBack) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}

@Composable
fun PlaceholderAction(
    @StringRes labelRes: Int,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = stringResource(labelRes))
    }
}

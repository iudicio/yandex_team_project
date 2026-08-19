package ru.practicum.android.diploma.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Accent,
    background = LightBackground,
    surface = LightBackground,
    onPrimary = DarkText,
    onBackground = LightText,
    onSurface = LightText,
)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = DarkText,
    onBackground = DarkText,
    onSurface = DarkText,
)

@Composable
fun DiplomaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}

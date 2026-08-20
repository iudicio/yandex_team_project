package ru.practicum.android.diploma.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val DiplomaTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = HeadlineSmallFontSize,
        lineHeight = HeadlineSmallLineHeight,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = BodyLargeFontSize,
        lineHeight = BodyLargeLineHeight,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = BodyMediumFontSize,
        lineHeight = BodyMediumLineHeight,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = LabelSmallFontSize,
        lineHeight = LabelSmallLineHeight,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = TitleMediumFontSize,
        lineHeight = TitleMediumLineHeight,
    ),
)

private val LightDiplomaColorScheme = lightColorScheme(
    primary = FigmaAccent,
    onPrimary = FigmaWhite,
    background = FigmaWhite,
    surface = FigmaWhite,
    surfaceVariant = FigmaSurfaceLight,
    onBackground = FigmaDark,
    onSurface = FigmaDark,
    onSurfaceVariant = FigmaSecondary,
    error = FigmaDestructive,
    onError = FigmaWhite,
)

private val DarkDiplomaColorScheme = darkColorScheme(
    primary = FigmaAccent,
    onPrimary = FigmaWhite,
    background = FigmaDark,
    surface = FigmaDark,
    surfaceVariant = FigmaSecondary,
    onBackground = FigmaWhite,
    onSurface = FigmaWhite,
    onSurfaceVariant = FigmaWhite,
    error = FigmaDestructive,
    onError = FigmaWhite,
)

@Composable
fun DiplomaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkDiplomaColorScheme else LightDiplomaColorScheme,
        typography = DiplomaTypography,
        content = content,
    )
}

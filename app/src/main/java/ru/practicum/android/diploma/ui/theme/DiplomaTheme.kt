package ru.practicum.android.diploma.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.practicum.android.diploma.R

private val DiplomaTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun DiplomaTheme(
    content: @Composable () -> Unit,
) {
    val background = colorResource(R.color.background_primary)
    val textPrimary = colorResource(R.color.text_primary)
    val surfaceVariant = colorResource(R.color.search_surface)
    val secondaryText = colorResource(R.color.search_hint)
    val accent = colorResource(R.color.accent)
    val destructive = colorResource(R.color.destructive)

    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = accent,
            background = background,
            surface = background,
            surfaceVariant = surfaceVariant,
            onBackground = textPrimary,
            onSurface = textPrimary,
            onSurfaceVariant = secondaryText,
            error = destructive,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = background,
            surface = background,
            surfaceVariant = surfaceVariant,
            onBackground = textPrimary,
            onSurface = textPrimary,
            onSurfaceVariant = secondaryText,
            error = destructive,
            onError = Color.White,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiplomaTypography,
        content = content,
    )
}

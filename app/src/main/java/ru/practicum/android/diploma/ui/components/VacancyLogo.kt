package ru.practicum.android.diploma.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.practicum.android.diploma.R

@Composable
fun VacancyLogo(logo: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    AsyncImage(
        model = logo,
        contentDescription = stringResource(R.string.search_company_logo_description),
        placeholder = painterResource(R.drawable.search_logo_placeholder),
        error = painterResource(R.drawable.search_logo_placeholder),
        fallback = painterResource(R.drawable.search_logo_placeholder),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, shape),
    )
}

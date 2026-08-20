package ru.practicum.android.diploma.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import ru.practicum.android.diploma.R

@Composable
fun VacancyLogo(
    logoUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cachePolicy: LogoCachePolicy = LogoCachePolicy.Default,
) {
    val placeholder = painterResource(R.drawable.search_logo_placeholder)
    val shape = RoundedCornerShape(12.dp)
    val request = ImageRequest.Builder(LocalContext.current)
        .data(logoUrl.takeUnless { cachePolicy == LogoCachePolicy.Hidden })
        .apply {
            if (cachePolicy == LogoCachePolicy.NetworkOnly) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
                networkCachePolicy(CachePolicy.ENABLED)
            }
        }
        .build()
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, shape),
    )
}

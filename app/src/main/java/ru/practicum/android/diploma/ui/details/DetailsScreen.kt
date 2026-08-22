package ru.practicum.android.diploma.ui.details

import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.search.formatSalary
import ru.practicum.android.diploma.presentation.details.DetailsError
import ru.practicum.android.diploma.presentation.details.DetailsUiState
import ru.practicum.android.diploma.ui.components.LogoCachePolicy
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.components.VacancyLogo

private const val DP_4 = 4
private const val DP_8 = 8
private const val DP_12 = 12
private const val DP_16 = 16
private const val DP_24 = 24
private const val DP_32 = 32
private const val DP_46 = 46
private const val DP_48 = 48
private const val DP_223 = 223
private const val SP_16 = 16
private const val SP_22 = 22
private const val SP_32 = 32
private const val SP_38 = 38
private const val HTML_LINE_SPACING_MULTIPLIER = 1.2f

@Composable
fun DetailsScreen(
    state: DetailsUiState,
    onBackPressed: () -> Unit,
    onShareClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onPhoneClicked: (String) -> Unit,
    onEmailClicked: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DetailsHeader(
            hasContent = state.vacancy != null,
            canShare = state.vacancy?.url?.isNotBlank() == true,
            isFavorite = state.isFavorite,
            onBackPressed = onBackPressed,
            onShareClicked = onShareClicked,
            onFavoriteClicked = onFavoriteClicked,
        )
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(error = state.error, onRetry = onRetry)
            state.vacancy != null -> VacancyContent(
                vacancy = state.vacancy,
                isOffline = state.isOffline,
                onPhoneClicked = onPhoneClicked,
                onEmailClicked = onEmailClicked,
            )
        }
    }
}

@Composable
private fun DetailsHeader(
    hasContent: Boolean,
    canShare: Boolean,
    isFavorite: Boolean,
    onBackPressed: () -> Unit,
    onShareClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
) {
    ScreenHeader(
        title = stringResource(R.string.details_toolbar_title),
        backActions = {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(DP_48.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.details_back_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            IconButton(
                onClick = onShareClicked,
                enabled = canShare,
                modifier = Modifier.size(DP_48.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.details_share_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(
                onClick = onFavoriteClicked,
                enabled = hasContent,
                modifier = Modifier.size(DP_48.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off,
                    ),
                    contentDescription = stringResource(
                        if (isFavorite) {
                            R.string.details_favorite_remove_description
                        } else {
                            R.string.details_favorite_add_description
                        },
                    ),
                    tint = if (isFavorite) Color.Unspecified else MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorContent(
    error: DetailsError,
    onRetry: () -> Unit,
) {
    val illustration: Int
    val message: String
    when (error) {
        DetailsError.NoInternet -> {
            illustration = R.drawable.search_no_internet
            message = stringResource(R.string.details_no_internet)
        }

        DetailsError.NotFound -> {
            illustration = R.drawable.details_empty
            message = stringResource(R.string.details_not_found)
        }

        DetailsError.Server -> {
            illustration = R.drawable.search_server_error
            message = stringResource(R.string.details_server_error)
        }
    }
    ErrorPlaceholder(
        illustration = illustration,
        message = message,
        onRetry = onRetry,
    )
}

@Composable
private fun ErrorPlaceholder(
    @DrawableRes illustration: Int,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DP_16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(DP_223.dp)
                .padding(horizontal = DP_16.dp),
        )
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = DP_46.dp, top = DP_16.dp, end = DP_46.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = DP_24.dp),
        ) {
            Text(text = stringResource(R.string.details_retry))
        }
    }
}

@Composable
private fun VacancyContent(
    vacancy: VacancyDetailResult,
    isOffline: Boolean,
    onPhoneClicked: (String) -> Unit,
    onEmailClicked: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DP_16.dp),
    ) {
        VacancyTitle(vacancy)
        EmployerCard(vacancy = vacancy, isOffline = isOffline)
        VacancyConditions(vacancy)
        vacancy.description.takeIf(String::isNotBlank)?.let { description ->
            HtmlText(
                html = description,
                modifier = Modifier.padding(top = DP_8.dp),
            )
        }
        KeySkills(vacancy.skills)
        ContactsSection(
            contacts = vacancy.contacts,
            onPhoneClicked = onPhoneClicked,
            onEmailClicked = onEmailClicked,
        )
        Spacer(modifier = Modifier.height(DP_24.dp))
    }
}

@Composable
private fun VacancyTitle(vacancy: VacancyDetailResult) {
    vacancy.name.takeIf(String::isNotBlank)?.let { name ->
        Text(
            text = name,
            modifier = Modifier.padding(top = DP_16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = SP_32.sp,
                lineHeight = SP_38.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
    Text(
        text = vacancy.salary.formatSalary(stringResource(R.string.details_salary_not_specified)),
        modifier = Modifier.padding(top = DP_4.dp),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
    )
    Spacer(modifier = Modifier.height(DP_24.dp))
}

@Composable
private fun EmployerCard(
    vacancy: VacancyDetailResult,
    isOffline: Boolean,
) {
    val employerName = vacancy.employer.name?.takeIf(String::isNotBlank)
    val location = vacancy.detailsLocation()
    val logoUrl = vacancy.employer.logo?.takeIf(String::isNotBlank)
    if (employerName == null && location == null && logoUrl == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DP_12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(DP_16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VacancyLogo(
            logoUrl = logoUrl,
            contentDescription = stringResource(R.string.details_company_logo_description),
            cachePolicy = if (isOffline) LogoCachePolicy.Hidden else LogoCachePolicy.Default,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = DP_12.dp),
        ) {
            employerName?.let { name ->
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = SP_22.sp),
                )
            }
            location?.let { address ->
                Text(
                    text = address,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(DP_24.dp))
}

@Composable
private fun VacancyConditions(vacancy: VacancyDetailResult) {
    vacancy.experience?.name?.takeIf(String::isNotBlank)?.let { experience ->
        DetailItem(
            label = stringResource(R.string.details_experience_label),
            value = experience,
        )
    }
    val employment = vacancy.employment?.name?.takeIf(String::isNotBlank)
    val schedule = vacancy.schedule?.name?.takeIf(String::isNotBlank)
    val combined = when {
        employment != null && schedule != null -> stringResource(
            R.string.details_employment_schedule,
            employment,
            schedule,
        )

        employment != null -> employment
        else -> schedule
    }
    combined?.let { conditions ->
        Text(
            text = conditions,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    Spacer(modifier = Modifier.height(DP_32.dp))
}

@Composable
private fun DetailItem(label: String, value: String) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    )
    Text(
        text = value,
        modifier = Modifier.padding(top = DP_4.dp, bottom = DP_8.dp),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun KeySkills(skills: List<String>) {
    if (skills.isEmpty()) return
    Spacer(modifier = Modifier.height(DP_24.dp))
    SectionTitle(stringResource(R.string.details_key_skills_title))
    skills.forEach { skill ->
        Text(
            text = stringResource(R.string.details_skill_item, skill),
            modifier = Modifier.padding(top = DP_4.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ContactsSection(
    contacts: Contacts?,
    onPhoneClicked: (String) -> Unit,
    onEmailClicked: (String) -> Unit,
) {
    val visibleContacts = contacts?.takeIf(Contacts::hasVisibleContent) ?: return
    Spacer(modifier = Modifier.height(DP_24.dp))
    SectionTitle(stringResource(R.string.details_contacts_title))
    visibleContacts.name?.takeIf(String::isNotBlank)?.let { name ->
        ContactItem(
            label = stringResource(R.string.details_contact_person),
            value = name,
        )
    }
    visibleContacts.email?.takeIf(String::isNotBlank)?.let { email ->
        ContactItem(
            label = stringResource(R.string.details_contact_email),
            value = email,
            onClick = { onEmailClicked(email) },
        )
    }
    visibleContacts.phones.forEach { phone ->
        ContactPhone(phone = phone, onPhoneClicked = onPhoneClicked)
    }
}

internal fun Contacts.hasVisibleContent(): Boolean =
    !name.isNullOrBlank() || !email.isNullOrBlank() || phones.any(Phone::hasVisibleContent)

internal fun Phone.hasVisibleContent(): Boolean = !formatted.isNullOrBlank() || !comment.isNullOrBlank()

@Composable
private fun ContactPhone(
    phone: Phone,
    onPhoneClicked: (String) -> Unit,
) {
    phone.formatted?.takeIf(String::isNotBlank)?.let { formatted ->
        ContactItem(
            label = stringResource(R.string.details_contact_phone),
            value = formatted,
            onClick = { onPhoneClicked(formatted) },
        )
    }
    phone.comment?.takeIf(String::isNotBlank)?.let { comment ->
        ContactItem(
            label = stringResource(R.string.details_contact_comment),
            value = comment,
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = SP_22.sp),
    )
}

@Composable
private fun ContactItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(top = DP_8.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        )
        Text(
            text = value,
            modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
            color = if (onClick == null) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.primary
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                textSize = SP_16.toFloat()
                typeface = Typeface.SANS_SERIF
                setLineSpacing(0f, HTML_LINE_SPACING_MULTIPLIER)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        },
    )
}

internal fun VacancyDetailResult.detailsLocation(): String? = address?.raw?.takeIf(String::isNotBlank)
    ?: address?.city?.takeIf(String::isNotBlank)
    ?: area?.name?.takeIf(String::isNotBlank)

package ru.practicum.android.diploma.ui.details

import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.presentation.details.DetailsUiState
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.theme.DiplomaTheme
import androidx.compose.foundation.clickable
import coil3.compose.AsyncImage


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
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.details_title),
            backActions = {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.arrow_description),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                IconButton(onClick = onShareClicked) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = stringResource(R.string.details_share),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onFavoriteClicked) {
                    Icon(
                        painter = painterResource(
                            if (state.isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off
                        ),
                        contentDescription = if (state.isFavorite) {
                            stringResource(R.string.details_favorite_remove)
                        } else {
                            stringResource(R.string.details_favorite_add)
                        },
                        tint = Color.Unspecified
                    )
                }
            }
        )

        when {
            state.isLoading -> {
                LoadingState()
            }

            state.error != null -> {
                ErrorState(
                    message = state.error,
                    onRetry = onRetry
                )
            }

            state.vacancy != null -> {
                VacancyContent(
                    vacancy = state.vacancy,
                    onPhoneClicked = onPhoneClicked,
                    onEmailClicked = onEmailClicked
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun VacancyContent(
    vacancy: VacancyDetailResult,
    onPhoneClicked: (String) -> Unit,
    onEmailClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = vacancy.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = formatSalary(vacancy.salary),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Плашка работодателя
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                if (vacancy.employer.logo.isNotBlank()) {

                    AsyncImage(
                        model = vacancy.employer.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.search_logo_placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = vacancy.employer.name ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = vacancy.address?.city ?: vacancy.area?.name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        vacancy.experience?.let { experience ->
            Text(
                text = stringResource(R.string.details_experience),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = experience.name ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val employmentSchedule = buildString {
            vacancy.employment?.let { append(it.name) }
            if (vacancy.employment != null && vacancy.schedule != null) {
                append(" • ")
            }
            vacancy.schedule?.let { append(it.name) }
        }

        if (employmentSchedule.isNotEmpty()) {
            Text(
                text = employmentSchedule,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        HtmlText(
            html = vacancy.description,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (vacancy.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.details_key_skills_header),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            vacancy.skills.forEach { skill ->
                Text(
                    text = "• $skill",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        vacancy.contacts?.let { contacts ->
            if (contacts.name.isNotBlank() || contacts.email.isNotBlank() || contacts.phones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.details_contacts_header),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (contacts.name.isNotBlank()) {
                    ContactItem(
                        label = stringResource(R.string.details_contact_person),
                        value = contacts.name
                    )
                }

                if (contacts.email.isNotBlank()) {
                    ContactItem(
                        label = stringResource(R.string.details_contact_email),
                        value = contacts.email,
                        onClick = { onEmailClicked(contacts.email) }
                    )
                }

                contacts.phones.forEach { phone ->
                    ContactItem(
                        label = stringResource(R.string.details_contact_phone),
                        value = phone.formatted,
                        onClick = { onPhoneClicked(phone.formatted) }
                    )
                    phone.comment?.let { comment ->
                        ContactItem(
                            label = stringResource(R.string.details_contact_comment),
                            value = comment
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun formatSalary(salary: ru.practicum.android.diploma.domain.search.Salary?): String {
    if (salary == null) return stringResource(R.string.salary_not_specified)

    val currencySymbol = when (salary.currency) {
        "RUB" -> "₽"
        "USD" -> "$"
        "EUR" -> "€"
        else -> salary.currency ?: ""
    }

    return when {
        salary.from != null && salary.to != null ->
            "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencySymbol".trim()

        salary.from != null ->
            "от ${formatNumber(salary.from)} $currencySymbol".trim()

        salary.to != null ->
            "до ${formatNumber(salary.to)} $currencySymbol".trim()

        else -> stringResource(R.string.salary_not_specified)
    }
}

private fun formatNumber(number: Int): String {
    return "%,d".format(number).replace(',', ' ')
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onBackground
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor.toArgb())
                textSize = 16f
                typeface = Typeface.SANS_SERIF
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) }
    )
}

@Composable
private fun ContactItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onClick != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onClick)
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    val mockState = DetailsUiState(
        isLoading = false,
        vacancy = VacancyDetailResult(
            id = "1",
            name = "Android-разработчик",
            description = "<h2>Описание</h2><p>Тестовое описание</p>",
            salary = ru.practicum.android.diploma.domain.search.Salary(
                from = 100000,
                to = 150000,
                currency = "RUB"
            ),
            address = ru.practicum.android.diploma.domain.details.Address(
                id = "1",
                city = "Москва",
                street = "Ленина",
                building = "1",
                raw = "Москва, ул. Ленина, 1"
            ),
            experience = ru.practicum.android.diploma.domain.details.BaseDetailData(
                id = "1",
                name = "От 1 года до 3 лет"
            ),
            schedule = ru.practicum.android.diploma.domain.details.BaseDetailData(
                id = "1",
                name = "Удаленная работа"
            ),
            employment = ru.practicum.android.diploma.domain.details.BaseDetailData(
                id = "1",
                name = "Полная занятость"
            ),
            contacts = ru.practicum.android.diploma.domain.details.Contacts(
                id = "1",
                name = "Иван Иванов",
                email = "ivan@yandex.ru",
                phones = listOf(
                    ru.practicum.android.diploma.domain.details.Phone(
                        comment = "Звонить с 10:00 до 19:00",
                        formatted = "+7 (999) 000-00-00"
                    )
                )
            ),
            employer = ru.practicum.android.diploma.domain.details.Employer(
                id = "1",
                name = "Яндекс",
                logo = ""
            ),
            area = ru.practicum.android.diploma.domain.filter.Area(
                id = 1,
                name = "Москва",
                parentId = null,
                areas = emptyList()
            ),
            skills = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Dagger/Hilt"),
            url = "https://hh.ru/vacancy/123456",
            industry = ru.practicum.android.diploma.domain.filter.Industry(
                id = "1",
                name = "IT"
            )
        ),
        isFavorite = false
    )

    DiplomaTheme {
        DetailsScreen(
            state = mockState,
            onBackPressed = {},
            onShareClicked = {},
            onFavoriteClicked = {},
            onPhoneClicked = {},
            onEmailClicked = {},
            onRetry = {}
        )
    }
}

package ru.practicum.android.diploma.ui.details

import android.graphics.Typeface
import android.widget.TextView
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
import coil3.compose.AsyncImage
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.Address
import ru.practicum.android.diploma.domain.details.BaseDetailData
import ru.practicum.android.diploma.domain.details.Contacts
import ru.practicum.android.diploma.domain.details.Employer
import ru.practicum.android.diploma.domain.details.Phone
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.domain.filter.Area
import ru.practicum.android.diploma.domain.filter.Industry
import ru.practicum.android.diploma.domain.search.Salary
import ru.practicum.android.diploma.presentation.details.DetailsUiState
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

private const val EMPLOYER_LOGO_SIZE = 48
private const val CONTENT_PADDING = 16
private const val TITLE_FONT_SIZE = 32
private const val TITLE_LINE_HEIGHT = 38
private const val EMPLOYER_NAME_FONT_SIZE = 22
private const val KEY_SKILLS_FONT_SIZE = 22
private const val HTML_TEXT_SIZE = 16f
private const val HTML_LINE_SPACING = 1.2f
private const val MOCK_SALARY_FROM = 100_000
private const val MOCK_SALARY_TO = 150_000

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
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = stringResource(R.string.details_error_massage),
                onRetry = onRetry
            )

            state.vacancy != null -> VacancyContent(
                vacancy = state.vacancy,
                onPhoneClicked = onPhoneClicked,
                onEmailClicked = onEmailClicked
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CONTENT_PADDING.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.details_empty),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(223.dp)
                .padding(horizontal = 16.dp),
        )
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp, top = 16.dp, end = 46.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
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
            .padding(horizontal = CONTENT_PADDING.dp)
    ) {
        VacancyHeader(vacancy = vacancy)
        EmployerCard(vacancy = vacancy)
        VacancyDetails(vacancy = vacancy)
        HtmlText(html = vacancy.description, modifier = Modifier.padding(top = 8.dp))
        KeySkills(skills = vacancy.skills)
        ContactsSection(
            contacts = vacancy.contacts,
            onPhoneClicked = onPhoneClicked,
            onEmailClicked = onEmailClicked
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VacancyHeader(vacancy: VacancyDetailResult) {
    Text(
        text = vacancy.name,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = TITLE_FONT_SIZE.sp,
            lineHeight = TITLE_LINE_HEIGHT.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = CONTENT_PADDING.dp)
    )
    Text(
        text = formatSalary(vacancy.salary),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun EmployerCard(vacancy: VacancyDetailResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(CONTENT_PADDING.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmployerLogo(employer = vacancy.employer)
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = vacancy.employer.name ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = EMPLOYER_NAME_FONT_SIZE.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = vacancy.address?.city ?: vacancy.area?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun EmployerLogo(employer: Employer) {
    Box(
        modifier = Modifier
            .size(EMPLOYER_LOGO_SIZE.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        if (employer.logo.isNotEmpty()) {
            AsyncImage(
                model = employer.logo,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside
            )
        }
    }
}

@Composable
private fun VacancyDetails(vacancy: VacancyDetailResult) {
    vacancy.experience?.let { experience ->
        DetailItem(label = stringResource(R.string.details_experience), value = experience.name ?: "")
    }
    val employmentSchedule = buildEmploymentSchedule(vacancy)
    if (employmentSchedule.isNotEmpty()) {
        Text(
            text = employmentSchedule,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun DetailItem(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun KeySkills(skills: List<String>) {
    if (skills.isEmpty()) return
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.details_key_skills_header),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = KEY_SKILLS_FONT_SIZE.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
    skills.forEach { skill ->
        Text(
            text = "• $skill",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ContactsSection(
    contacts: Contacts?,
    onPhoneClicked: (String) -> Unit,
    onEmailClicked: (String) -> Unit
) {
    if (contacts == null) return
    val hasContacts = contacts.name.isNotBlank() ||
        contacts.email.isNotBlank() ||
        contacts.phones.isNotEmpty()
    if (!hasContacts) return
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.details_contacts_header),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = KEY_SKILLS_FONT_SIZE.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
    if (contacts.name.isNotBlank()) {
        ContactItem(label = stringResource(R.string.details_contact_person), value = contacts.name)
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

@Composable
private fun buildEmploymentSchedule(vacancy: VacancyDetailResult): String {
    return buildString {
        vacancy.employment?.let { append(it.name) }
        if (vacancy.employment != null && vacancy.schedule != null) {
            append(" • ")
        }
        vacancy.schedule?.let { append(it.name) }
    }
}

@Composable
private fun formatSalary(salary: Salary?): String {
    if (salary == null) return stringResource(R.string.salary_not_specified)
    val currencySymbol = when (salary.currency) {
        "RUB" -> "₽"
        "USD" -> "$"
        "EUR" -> "€"
        else -> salary.currency.orEmpty()
    }
    return when {
        salary.from != null && salary.to != null ->
            "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencySymbol".trim()

        salary.from != null -> "от ${formatNumber(salary.from)} $currencySymbol".trim()
        salary.to != null -> "до ${formatNumber(salary.to)} $currencySymbol".trim()
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
                textSize = HTML_TEXT_SIZE
                typeface = Typeface.SANS_SERIF
                setLineSpacing(0f, HTML_LINE_SPACING)
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
            salary = Salary(from = MOCK_SALARY_FROM, to = MOCK_SALARY_TO, currency = "RUB"),
            address = Address(
                id = "1",
                city = "Москва",
                street = "Ленина",
                building = "1",
                raw = "Москва, ул. Ленина, 1"
            ),
            experience = BaseDetailData(id = "1", name = "От 1 года до 3 лет"),
            schedule = BaseDetailData(id = "1", name = "Удаленная работа"),
            employment = BaseDetailData(id = "1", name = "Полная занятость"),
            contacts = Contacts(
                id = "1",
                name = "Иван Иванов",
                email = "ivan@yandex.ru",
                phones = listOf(Phone(comment = "Звонить с 10:00 до 19:00", formatted = "+7 (999) 000-00-00"))
            ),
            employer = Employer(id = "1", name = "Яндекс", logo = ""),
            area = Area(id = 1, name = "Москва", parentId = null, areas = emptyList()),
            skills = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Dagger/Hilt"),
            url = "https://hh.ru/vacancy/123456",
            industry = Industry(id = "1", name = "IT")
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

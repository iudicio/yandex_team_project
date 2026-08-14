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
import ru.practicum.android.diploma.ui.components.ScreenHeader
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

data class VacancyDetailsMock(
    val id: String,
    val title: String,
    val salary: String,
    val employerName: String,
    val employerLogoUrl: String?,
    val city: String,
    val experience: String,
    val employment: String,
    val schedule: String,
    val description: String,
    val keySkills: List<String>,
    val contactName: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val contactComment: String?,
)

@Composable
fun DetailsScreen(
    vacancy: VacancyDetailsMock,
    onBackPressed: () -> Unit,
    onShareClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    isFavorite: Boolean,
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
                    modifier = Modifier
                        .size(48.dp)

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
                            if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off
                        ),
                        contentDescription = if (isFavorite) {
                            stringResource(R.string.details_favorite_remove)
                        } else {
                            stringResource(R.string.details_favorite_add)
                        },
                        tint = Color.Unspecified
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = vacancy.salary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Плашка раб/д. -->
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
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = vacancy.employerName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = vacancy.city,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } // <--

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.details_experience),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vacancy.experience,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.details_employment_schedule,
                    vacancy.employment,
                    vacancy.schedule
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            HtmlText(
                html = vacancy.description,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (vacancy.keySkills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.details_key_skills_header),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                vacancy.keySkills.forEach { skill ->
                    Text(
                        text = "• $skill",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (vacancy.contactName != null || vacancy.contactEmail != null || vacancy.contactPhone != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.details_contacts_header),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                vacancy.contactName?.let {
                    ContactItem(label = stringResource(R.string.details_contact_person), value = it)
                }
                vacancy.contactEmail?.let {
                    ContactItem(label = stringResource(R.string.details_contact_email), value = it)
                }
                vacancy.contactPhone?.let {
                    ContactItem(label = stringResource(R.string.details_contact_phone), value = it)
                }
                vacancy.contactComment?.let {
                    ContactItem(label = stringResource(R.string.details_contact_comment), value = it)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
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
private fun ContactItem(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    val mockVacancy = VacancyDetailsMock(
        id = "1",
        title = "Android-разработчик",
        salary = "от 100 000 до 150 000 ₽",
        employerName = "Яндекс",
        employerLogoUrl = null,
        city = "Москва",
        experience = "От 1 года до 3 лет",
        employment = "Полная занятость",
        schedule = "Удаленная работа",
        description = stringResource(R.string.mock_vacancy_description),
        keySkills = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Dagger/Hilt"),
        contactName = "Иван Иванов",
        contactEmail = "ivan@yandex.ru",
        contactPhone = "+7 (999) 000-00-00",
        contactComment = "Звонить с 10:00 до 19:00",
    )
    DiplomaTheme {
        DetailsScreen(
            vacancy = mockVacancy,
            onBackPressed = {},
            onShareClicked = {},
            onFavoriteClicked = {},
            isFavorite = false
        )
    }
}

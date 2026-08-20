package ru.practicum.android.diploma.ui.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.presentation.details.DetailsEvent
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.ui.common.ComposeDestinationFragment
import ru.practicum.android.diploma.ui.common.VACANCY_ID_ARGUMENT

class DetailsFragment : ComposeDestinationFragment() {

    private val vacancyId: String by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(requireArguments().getString(VACANCY_ID_ARGUMENT)?.takeIf(String::isNotBlank)) {
            "Required argument $VACANCY_ID_ARGUMENT is missing"
        }
    }

    private val viewModel: DetailsViewModel by viewModel {
        parametersOf(vacancyId)
    }

    @Composable
    override fun DestinationContent(navController: NavController) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        DetailsScreen(
            state = state,
            onBackPressed = viewModel::onBackPressed,
            onShareClicked = viewModel::onShareClicked,
            onFavoriteClicked = viewModel::onFavoriteClicked,
            onPhoneClicked = viewModel::onPhoneClicked,
            onEmailClicked = viewModel::onEmailClicked,
            onRetry = viewModel::retry,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect(::handleEvent)
            }
        }
    }

    private fun handleEvent(event: DetailsEvent) {
        when (event) {
            DetailsEvent.NavigateBack -> findNavController().navigateUp()
            is DetailsEvent.ShareVacancy -> shareVacancy(event.vacancy)
            is DetailsEvent.DialPhone -> openDialer(event.phone)
            is DetailsEvent.SendEmail -> openEmail(event.email)
            DetailsEvent.FavoriteUpdateFailed -> showToast(R.string.details_favorite_update_error)
        }
    }

    private fun shareVacancy(vacancy: VacancyDetailResult) {
        val text = buildString {
            vacancy.name.takeIf(String::isNotBlank)?.let { name ->
                appendLine(name)
            }
            append(vacancy.url)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startSafely(
            intent = Intent.createChooser(intent, getString(R.string.details_share_chooser_title)),
            errorMessage = R.string.details_share_intent_error,
        )
    }

    private fun openDialer(phone: String) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", phone, null),
        )
        startSafely(intent = intent, errorMessage = R.string.details_phone_intent_error)
    }

    private fun openEmail(email: String) {
        val intent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", email, null),
        )
        startSafely(intent = intent, errorMessage = R.string.details_email_intent_error)
    }

    private fun startSafely(intent: Intent, errorMessage: Int) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            showToast(errorMessage)
        }
    }

    private fun showToast(message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

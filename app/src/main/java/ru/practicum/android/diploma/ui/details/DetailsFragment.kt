package ru.practicum.android.diploma.ui.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.presentation.details.DetailsEvent
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class DetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModel {
        parametersOf(requireArguments().getString(ARG_VACANCY_ID).orEmpty())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        id = R.id.details_compose_view
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DiplomaTheme {
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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEvents()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is DetailsEvent.NavigateBack -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        is DetailsEvent.ShareVacancy -> {
                            shareVacancy(event.vacancy)
                        }

                        is DetailsEvent.DialPhone -> {
                            dialPhone(event.phone)
                        }

                        is DetailsEvent.SendEmail -> {
                            sendEmail(event.email)
                        }
                    }
                }
            }
        }
    }

    private fun shareVacancy(vacancy: VacancyDetailResult) {
        val shareText = buildString {
            append(vacancy.name)
            append("\n\n")
            vacancy.salary?.let { salary ->
                if (salary.from != null || salary.to != null) {
                    append("Зарплата: ")
                    append(formatSalary(salary))
                    append("\n")
                }
            }
            append(vacancy.employer.name)
            vacancy.address?.city?.let { city ->
                append(", ")
                append(city)
            }
            append("\n")
            append(vacancy.url)
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_vacancy)))
    }

    private fun dialPhone(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$phone".toUri()
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w("DetailsFragment", "No activity to handle intent", e)
            Toast.makeText(
                requireContext(),
                R.string.details_phone_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$email".toUri()
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w("DetailsFragment", "No activity to handle intent", e)
            Toast.makeText(
                requireContext(),
                R.string.details_email_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatSalary(salary: ru.practicum.android.diploma.domain.search.Salary): String {
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
            else -> getString(R.string.salary_not_specified)
        }
    }

    private fun formatNumber(number: Int): String {
        return "%,d".format(number).replace(',', ' ')
    }

    companion object {
        const val ARG_VACANCY_ID = "vacancy_id"
    }
}

package ru.practicum.android.diploma.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.di.appContainer
import ru.practicum.android.diploma.domain.details.FavoritesRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailRepository
import ru.practicum.android.diploma.domain.details.VacancyDetailResult
import ru.practicum.android.diploma.presentation.details.DetailsEvent
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.ui.theme.DiplomaTheme

class DetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModels {
        val container = requireContext().appContainer
        DetailsViewModelFactory(
            vacancyId = arguments?.getString(ARG_VACANCY_ID) ?: "",
            vacancyDetailRepository = container.vacancyDetailRepository,
            favoritesRepository = container.favoritesRepository,
        )
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
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        } catch (e: Exception) {
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
                data = Uri.parse("mailto:$email")
            }
            startActivity(intent)
        } catch (e: Exception) {
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
            else -> salary.currency ?: ""
        }

        return when {
            salary.from != null && salary.to != null ->
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencySymbol".trim()
            salary.from != null ->
                "от ${formatNumber(salary.from)} $currencySymbol".trim()
            salary.to != null ->
                "до ${formatNumber(salary.to)} $currencySymbol".trim()
            else -> getString(R.string.salary_not_specified)
        }
    }

    private fun formatNumber(number: Int): String {
        return "%,d".format(number).replace(',', ' ')
    }

    companion object {
        const val ARG_VACANCY_ID = "vacancy_id"

        fun newInstance(vacancyId: String): DetailsFragment {
            return DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VACANCY_ID, vacancyId)
                }
            }
        }
    }
}

private class DetailsViewModelFactory(
    private val vacancyId: String,
    private val vacancyDetailRepository: VacancyDetailRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == DetailsViewModel::class.java) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return modelClass.cast(
            DetailsViewModel(
                savedStateHandle = extras.createSavedStateHandle(),
                vacancyId = vacancyId,
                vacancyDetailRepository = vacancyDetailRepository,
                favoritesRepository = favoritesRepository,
            ),
        )
    }
}

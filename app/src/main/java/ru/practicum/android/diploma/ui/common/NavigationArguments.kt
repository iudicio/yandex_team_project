package ru.practicum.android.diploma.ui.common

import android.os.Bundle

const val VACANCY_ID_ARGUMENT = "vacancyId"
const val FILTER_APPLIED_RESULT_KEY = "filterApplied"

fun vacancyArguments(vacancyId: String): Bundle = Bundle().apply {
    putString(VACANCY_ID_ARGUMENT, vacancyId)
}

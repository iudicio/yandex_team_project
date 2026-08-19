package ru.practicum.android.diploma.ui.common

import android.os.Bundle

const val VACANCY_ID_ARGUMENT = "vacancyId"

fun vacancyArguments(vacancyId: String): Bundle = Bundle().apply {
    putString(VACANCY_ID_ARGUMENT, vacancyId)
}

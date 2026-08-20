package ru.practicum.android.diploma.domain.search

import java.text.NumberFormat
import java.util.Locale

private const val NON_BREAKING_SPACE = '\u00A0'

fun Salary?.formatSalary(missingSalaryText: String): String {
    if (this == null || from == null && to == null) return missingSalaryText

    val currencySuffix = currency.toCurrencySymbol()
        .takeIf(String::isNotEmpty)
        ?.let { formattedCurrency -> " $formattedCurrency" }
        .orEmpty()
    val formattedFrom = from?.formatNumber()
    val formattedTo = to?.formatNumber()

    return when {
        formattedFrom != null && formattedTo != null -> "от $formattedFrom до $formattedTo$currencySuffix"
        formattedFrom != null -> "от $formattedFrom$currencySuffix"
        else -> "до $formattedTo$currencySuffix"
    }
}

private fun Int.formatNumber(): String = NumberFormat
    .getIntegerInstance(Locale.forLanguageTag("ru-RU"))
    .format(this)
    .replace(' ', NON_BREAKING_SPACE)

private fun String?.toCurrencySymbol(): String = when (this?.uppercase(Locale.ROOT)) {
    "RUR", "RUB" -> "₽"
    "BYR" -> "Br"
    "USD" -> "$"
    "EUR" -> "€"
    "KZT" -> "₸"
    "UAH" -> "₴"
    "AZN" -> "₼"
    "UZS" -> "сум"
    "GEL" -> "₾"
    "KGT" -> "сом"
    null -> ""
    else -> this
}

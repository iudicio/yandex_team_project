package ru.practicum.android.diploma.domain.search

import java.text.NumberFormat
import java.util.Locale

private const val NON_BREAKING_SPACE = '\u00A0'

fun Salary.format(): String {
    val formattedCurrency = currency.toCurrencySymbol()
    val currencySuffix = formattedCurrency.takeIf(String::isNotEmpty)?.let { " $it" }.orEmpty()
    val formattedFrom = from?.formatNumber()
    val formattedTo = to?.formatNumber()

    return when {
        formattedFrom != null && formattedTo != null && from == to -> "$formattedFrom$currencySuffix"
        formattedFrom != null && formattedTo != null -> "от $formattedFrom до $formattedTo$currencySuffix"
        formattedFrom != null -> "от $formattedFrom$currencySuffix"
        formattedTo != null -> "до $formattedTo$currencySuffix"
        else -> "Зарплата не указана"
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

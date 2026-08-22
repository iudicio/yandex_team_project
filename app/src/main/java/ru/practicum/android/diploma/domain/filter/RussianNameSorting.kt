package ru.practicum.android.diploma.domain.filter

import java.text.Collator
import java.util.Locale

internal fun <T> Iterable<T>.sortedByRussianName(name: (T) -> String): List<T> {
    val collator = Collator.getInstance(Locale.forLanguageTag(RUSSIAN_LANGUAGE_TAG)).apply {
        strength = Collator.PRIMARY
    }
    return sortedWith { left, right -> collator.compare(name(left), name(right)) }
}

private const val RUSSIAN_LANGUAGE_TAG = "ru"

package ru.practicum.android.diploma.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun <T> debounce(
    delayMillis: Long,
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit,
): (T) -> Unit {
    require(delayMillis >= 0L) { "Debounce delay must not be negative" }

    var debounceJob: Job? = null
    return { value ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(delayMillis)
            action(value)
        }
    }
}

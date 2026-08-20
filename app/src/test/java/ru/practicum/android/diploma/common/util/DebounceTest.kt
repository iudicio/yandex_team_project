package ru.practicum.android.diploma.common.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebounceTest {

    @Test
    fun `debounce invokes only the latest value after delay`() = runTest {
        val values = mutableListOf<String>()
        val callback = debounce<String>(
            delayMillis = DEBOUNCE_DELAY,
            coroutineScope = this,
        ) { value -> values += value }

        callback("first")
        advanceTimeBy(DEBOUNCE_DELAY - 1L)
        assertTrue(values.isEmpty())

        callback("second")
        advanceUntilIdle()

        assertEquals(listOf("second"), values)
    }

    @Test
    fun `debounce rejects a negative delay`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            debounce<String>(
                delayMillis = -1L,
                coroutineScope = this,
            ) { }
        }
    }

    private companion object {
        const val DEBOUNCE_DELAY = 300L
    }
}

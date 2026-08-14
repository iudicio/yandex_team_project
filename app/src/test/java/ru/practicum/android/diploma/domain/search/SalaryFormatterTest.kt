package ru.practicum.android.diploma.domain.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryFormatterTest {

    @Test
    fun `formats missing salary`() {
        assertEquals("Зарплата не указана", Salary().format())
    }

    @Test
    fun `formats salary bounds with non-breaking thousands and currency`() {
        val space = '\u00A0'

        assertEquals("от 100${space}000 ₽", Salary(from = 100_000, currency = "RUR").format())
        assertEquals("до 80${space}000 €", Salary(to = 80_000, currency = "EUR").format())
        assertEquals(
            "от 40${space}000 до 80${space}000 $",
            Salary(from = 40_000, to = 80_000, currency = "USD").format(),
        )
    }

    @Test
    fun `formats equal bounds as a single value`() {
        assertEquals("100${'\u00A0'}000 ₸", Salary(from = 100_000, to = 100_000, currency = "KZT").format())
    }

    @Test
    fun `preserves unknown currency`() {
        assertEquals("от 123 XYZ", Salary(from = 123, currency = "XYZ").format())
    }
}

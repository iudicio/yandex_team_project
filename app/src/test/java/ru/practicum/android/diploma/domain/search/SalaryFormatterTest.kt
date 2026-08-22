package ru.practicum.android.diploma.domain.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryFormatterTest {

    @Test
    fun `missing salary uses caller text`() {
        assertEquals("зарплата не указана", null.formatSalary("зарплата не указана"))
        assertEquals(
            "Уровень зарплаты не указан",
            Salary(currency = "RUR").formatSalary("Уровень зарплаты не указан"),
        )
    }

    @Test
    fun `formats lower upper and both boundaries`() {
        assertEquals("от 100 000 ₽", Salary(from = 100_000, currency = "RUR").formatted())
        assertEquals("до 150 000 $", Salary(to = 150_000, currency = "USD").formatted())
        assertEquals(
            "от 100 000 до 150 000 €",
            Salary(from = 100_000, to = 150_000, currency = "EUR").formatted(),
        )
    }

    @Test
    fun `equal boundaries still render from and to`() {
        assertEquals(
            "от 120 000 до 120 000 ₽",
            Salary(from = 120_000, to = 120_000, currency = "RUB").formatted(),
        )
    }

    @Test
    fun `supports every documented currency`() {
        val expected = mapOf(
            "RUR" to "₽",
            "RUB" to "₽",
            "BYR" to "Br",
            "USD" to "$",
            "EUR" to "€",
            "KZT" to "₸",
            "UAH" to "₴",
            "AZN" to "₼",
            "UZS" to "сум",
            "GEL" to "₾",
            "KGT" to "сом",
        )

        expected.forEach { (currency, symbol) ->
            assertEquals("от 1 $symbol", Salary(from = 1, currency = currency).formatted())
        }
    }

    @Test
    fun `unknown currency is displayed as received`() {
        assertEquals("от 1 AMD", Salary(from = 1, currency = "AMD").formatted())
    }

    private fun Salary.formatted(): String = formatSalary("missing").replace('\u00A0', ' ')
}

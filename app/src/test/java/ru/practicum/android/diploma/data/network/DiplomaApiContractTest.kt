package ru.practicum.android.diploma.data.network

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Query

class DiplomaApiContractTest {

    @Test
    fun `vacancy search uses documented endpoint and filter query names`() {
        val method = DiplomaApi::class.java.declaredMethods
            .first { candidate -> candidate.name == "searchVacancies" }
        val get = requireNotNull(method.getAnnotation(GET::class.java))

        assertEquals("vacancies", get.value)
        assertEquals(
            listOf(
                "text",
                "area",
                "industry",
                "salary",
                "page",
                "only_with_salary",
            ),
            method.parameterAnnotations.mapNotNull { annotations ->
                annotations.filterIsInstance<Query>().singleOrNull()?.value
            },
        )
    }
}

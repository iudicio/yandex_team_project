package ru.practicum.android.diploma.data.network.dto

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogDtoTest {

    @Test
    fun `areas accept null root parent and both parent id field spellings`() {
        val json = """
            [
              {
                "id": 1,
                "name": "Россия",
                "parentId": null,
                "areas": [
                  {"id": 10, "name": "Москва", "parent_id": 1, "areas": []}
                ]
              }
            ]
        """.trimIndent()

        val areas: List<AreaDto> = Gson().fromJson(json, object : TypeToken<List<AreaDto>>() {}.type)

        assertNull(areas.single().parentId)
        assertEquals(1, areas.single().areas.orEmpty().single().parentId)
    }

    @Test
    fun `industry response keeps nullable fields for safe repository mapping`() {
        val json = """
            [
              {"id": 7, "name": "IT"},
              {"id": null, "name": null}
            ]
        """.trimIndent()

        val industries: List<IndustryDto> =
            Gson().fromJson(json, object : TypeToken<List<IndustryDto>>() {}.type)

        assertEquals(IndustryDto(id = 7, name = "IT"), industries.first())
        assertEquals(IndustryDto(id = null, name = null), industries.last())
    }
}

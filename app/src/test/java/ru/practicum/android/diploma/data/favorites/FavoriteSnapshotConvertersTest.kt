package ru.practicum.android.diploma.data.favorites

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.practicum.android.diploma.data.db.entity.FavoritePhoneSnapshot

class FavoriteSnapshotConvertersTest {
    private val converters = FavoriteSnapshotConverters()

    @Test
    fun `phones survive json round trip`() {
        val phones = listOf(
            FavoritePhoneSnapshot(comment = "После 10", formatted = "+7 999 000-00-00"),
            FavoritePhoneSnapshot(comment = null, formatted = "+7 999 111-11-11"),
        )

        assertEquals(phones, converters.jsonToPhones(converters.phonesToJson(phones)))
    }

    @Test
    fun `skills survive json round trip`() {
        val skills = listOf("Kotlin", "Room")

        assertEquals(skills, converters.jsonToSkills(converters.skillsToJson(skills)))
    }
}

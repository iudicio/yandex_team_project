package ru.practicum.android.diploma.data.favorites

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.practicum.android.diploma.data.db.entity.FavoritePhoneSnapshot

class FavoriteSnapshotConverters {
    private val gson = Gson()

    @TypeConverter
    fun phonesToJson(phones: List<FavoritePhoneSnapshot>): String = gson.toJson(phones)

    @TypeConverter
    fun jsonToPhones(json: String): List<FavoritePhoneSnapshot> = gson
        .fromJson(json, Array<FavoritePhoneSnapshot?>::class.java)
        ?.mapNotNull { phone -> phone }
        .orEmpty()

    @TypeConverter
    fun skillsToJson(skills: List<String>): String = gson.toJson(skills)

    @TypeConverter
    fun jsonToSkills(json: String): List<String> = gson
        .fromJson(json, Array<String?>::class.java)
        ?.mapNotNull { skill -> skill?.takeIf(String::isNotBlank) }
        .orEmpty()
}

package ru.practicum.android.diploma.domain.favorites

//Позже заменю на БД
interface FavoritesModel {
    fun getFavoriteIds(): List<String>
}

class InMemoryFavoritesModel(
    private val favoriteIds: List<String> = emptyList(),
) : FavoritesModel {

    override fun getFavoriteIds(): List<String> = favoriteIds
}

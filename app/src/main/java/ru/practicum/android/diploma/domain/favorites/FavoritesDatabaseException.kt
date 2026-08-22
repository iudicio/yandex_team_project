package ru.practicum.android.diploma.domain.favorites

class FavoritesDatabaseException(
    cause: Throwable,
) : Exception("Unable to access favorite vacancies", cause)

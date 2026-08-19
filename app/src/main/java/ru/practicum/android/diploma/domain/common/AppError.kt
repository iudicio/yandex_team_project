package ru.practicum.android.diploma.domain.common

sealed interface AppError {
    data object NoInternet : AppError

    data object Unauthorized : AppError

    data object NotFound : AppError

    data class Server(val statusCode: Int) : AppError

    data class Database(val cause: Throwable) : AppError

    data class Unknown(val cause: Throwable) : AppError
}

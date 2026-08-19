package ru.practicum.android.diploma.data.common

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import ru.practicum.android.diploma.data.network.NoInternetException
import ru.practicum.android.diploma.domain.common.AppError
import java.io.IOException
import java.net.UnknownHostException

object AppErrorMapper {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is CancellationException -> throw throwable
        is NoInternetException, is UnknownHostException, is IOException -> AppError.NoInternet
        is HttpException -> mapHttpException(throwable)
        is SQLiteException -> AppError.Database(cause = throwable)
        else -> AppError.Unknown(cause = throwable)
    }

    private fun mapHttpException(exception: HttpException): AppError = when (exception.code()) {
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> AppError.Unauthorized
        HTTP_NOT_FOUND -> AppError.NotFound
        in HTTP_SERVER_ERROR_RANGE -> AppError.Server(statusCode = exception.code())
        else -> AppError.Unknown(cause = exception)
    }

    private const val HTTP_UNAUTHORIZED = 401
    private const val HTTP_FORBIDDEN = 403
    private const val HTTP_NOT_FOUND = 404
    private const val HTTP_SERVER_ERROR_START = 500
    private const val HTTP_SERVER_ERROR_END = 599
    private val HTTP_SERVER_ERROR_RANGE = HTTP_SERVER_ERROR_START..HTTP_SERVER_ERROR_END
}

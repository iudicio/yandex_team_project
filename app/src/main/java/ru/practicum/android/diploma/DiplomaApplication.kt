package ru.practicum.android.diploma

import android.app.Application
import ru.practicum.android.diploma.di.AppContainer

class DiplomaApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

package com.example.movie

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.movie.core.di.databaseModule
import com.example.movie.core.di.networkModule
import com.example.movie.core.di.repositoryModule
import com.example.movie.core.di.viewModelModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(
            databaseModule,
            networkModule,
            repositoryModule,
            viewModelModule
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Showtime"
    ) {
        App()
    }
}
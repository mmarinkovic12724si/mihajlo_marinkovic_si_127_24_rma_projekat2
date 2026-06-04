package com.example.movie.core.di

import com.example.movie.core.network.HttpClientFactory
import com.example.movie.core.network.MoviesApi
import com.example.movie.core.network.MoviesApiImpl
import com.example.movie.core.network.ShowtimeApi
import com.example.movie.core.network.ShowtimeApiImpl
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {

    single<HttpClient> {
        HttpClientFactory.create()
    }

    single<MoviesApi> {
        MoviesApiImpl(
            httpClient = get()
        )
    }

    single<ShowtimeApi> {
        ShowtimeApiImpl(
            httpClient = get()
        )
    }

    single {
        MoviesApiProvider(
            moviesApi = get()
        )
    }

    single {
        ShowtimeApiProvider(
            showtimeApi = get()
        )
    }
}
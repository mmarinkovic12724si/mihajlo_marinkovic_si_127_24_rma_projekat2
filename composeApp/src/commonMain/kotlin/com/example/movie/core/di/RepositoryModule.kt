package com.example.movie.core.di

import com.example.movie.cinema.data.repository.MoviesRepositoryImpl
import com.example.movie.cinema.domain.repository.MoviesRepository
import com.example.movie.cinema.domain.service.GetGenresService
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.cinema.domain.service.GetMovieDetailsService
import com.example.movie.cinema.domain.service.GetMoviesService
import com.example.movie.core.auth.data.repository.AuthRepositoryImpl
import com.example.movie.core.auth.data.repository.MovieListsRepositoryImpl
import com.example.movie.core.auth.domain.repository.AuthRepository
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import com.example.movie.core.quiz.data.repository.QuizRepositoryImpl
import com.example.movie.core.quiz.domain.repository.QuizRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<MoviesRepository> {
        MoviesRepositoryImpl(
            moviesApi = get<MoviesApiProvider>().moviesApi,
            movieDao = get()
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            showtimeApi = get<ShowtimeApiProvider>().showtimeApi,
            tokenStorage = get(),
            favoriteDao = get(),
            watchlistDao = get()
        )
    }

    single<MovieListsRepository> {
        MovieListsRepositoryImpl(
            showtimeApi = get<ShowtimeApiProvider>().showtimeApi,
            authRepository = get(),
            movieDao = get(),
            favoriteDao = get(),
            watchlistDao = get()
        )
    }

    single<QuizRepository> {
        QuizRepositoryImpl(
            moviesApi = get<MoviesApiProvider>().moviesApi,
            movieDao = get(),
            quizStatsDao = get()
        )
    }

    factory<GetMoviesService> {
        GetMoviesService(
            moviesRepository = get()
        )
    }

    factory<GetMovieDetailsService> {
        GetMovieDetailsService(
            moviesRepository = get()
        )
    }

    factory<GetGenresService> {
        GetGenresService(
            moviesRepository = get()
        )
    }

    factory<GetImageBaseService> {
        GetImageBaseService(
            moviesRepository = get()
        )
    }
}

class MoviesApiProvider(
    val moviesApi: com.example.movie.core.network.MoviesApi
)

class ShowtimeApiProvider(
    val showtimeApi: com.example.movie.core.network.ShowtimeApi
)
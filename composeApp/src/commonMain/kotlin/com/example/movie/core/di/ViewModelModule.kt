package com.example.movie.core.di

import com.example.movie.cinema.domain.service.GetGenresService
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.cinema.domain.service.GetMovieDetailsService
import com.example.movie.cinema.domain.service.GetMoviesService
import com.example.movie.cinema.ui.details.DetailsViewModel
import com.example.movie.cinema.ui.start_screen.StartScreenViewModel
import com.example.movie.core.auth.domain.repository.AuthRepository
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import com.example.movie.core.auth.ui.AuthViewModel
import com.example.movie.core.database.dao.FavoriteDao
import com.example.movie.core.database.dao.QuizStatsDao
import com.example.movie.core.database.dao.WatchlistDao
import com.example.movie.core.lists.ui.MovieCollectionViewModel
import com.example.movie.core.profile.ui.ProfileViewModel
import com.example.movie.core.quiz.domain.repository.QuizRepository
import com.example.movie.core.quiz.ui.QuizViewModel
import org.koin.dsl.module

val viewModelModule = module {

    factory<AuthViewModel> {
        AuthViewModel(
            authRepository = get<AuthRepository>()
        )
    }

    factory<ProfileViewModel> {
        ProfileViewModel(
            authRepository = get<AuthRepository>(),
            favoriteDao = get<FavoriteDao>(),
            watchlistDao = get<WatchlistDao>(),
            quizStatsDao = get<QuizStatsDao>(),
            movieListsRepository = get<MovieListsRepository>()
        )
    }

    factory<MovieCollectionViewModel> {
        MovieCollectionViewModel(
            movieListsRepository = get<MovieListsRepository>(),
            getImageBaseService = get<GetImageBaseService>()
        )
    }

    factory<QuizViewModel> {
        QuizViewModel(
            quizRepository = get<QuizRepository>(),
            getImageBaseService = get<GetImageBaseService>()
        )
    }

    single<StartScreenViewModel> {
        StartScreenViewModel(
            getMoviesService = get<GetMoviesService>(),
            getImageBaseService = get<GetImageBaseService>(),
            getGenresService = get<GetGenresService>()
        )
    }

    factory<DetailsViewModel> {
        DetailsViewModel(
            getMovieDetailsService = get<GetMovieDetailsService>(),
            getImageBaseService = get<GetImageBaseService>(),
            movieListsRepository = get<MovieListsRepository>()
        )
    }
}
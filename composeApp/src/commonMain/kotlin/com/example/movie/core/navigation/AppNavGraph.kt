package com.example.movie.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.movie.cinema.ui.details.DetailScreen
import com.example.movie.cinema.ui.details.DetailsViewModel
import com.example.movie.cinema.ui.filters.FilterScreen
import com.example.movie.cinema.ui.start_screen.StartScreen
import com.example.movie.cinema.ui.start_screen.StartScreenEffect
import com.example.movie.cinema.ui.start_screen.StartScreenIntent
import com.example.movie.cinema.ui.start_screen.StartScreenViewModel
import com.example.movie.core.auth.ui.AuthEffect
import com.example.movie.core.auth.ui.AuthScreen
import com.example.movie.core.auth.ui.AuthViewModel
import com.example.movie.core.lists.ui.MovieCollectionEffect
import com.example.movie.core.lists.ui.MovieCollectionScreen
import com.example.movie.core.lists.ui.MovieCollectionType
import com.example.movie.core.lists.ui.MovieCollectionViewModel
import com.example.movie.core.profile.ui.ProfileEffect
import com.example.movie.core.profile.ui.ProfileScreen
import com.example.movie.core.profile.ui.ProfileViewModel
import com.example.movie.core.quiz.ui.QuizEffect
import com.example.movie.core.quiz.ui.QuizScreen
import com.example.movie.core.quiz.ui.QuizViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.AUTH
    ) {
        composable(route = AppRoutes.AUTH) {
            val authViewModel: AuthViewModel = koinInject()
            val authState by authViewModel.state.collectAsState()

            LaunchedEffect(authViewModel) {
                authViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        AuthEffect.OpenMovies -> {
                            navController.navigate(AppRoutes.MOVIES_ROOT) {
                                popUpTo(AppRoutes.AUTH) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            }

            AuthScreen(
                state = authState,
                onIntent = authViewModel::onIntent
            )
        }

        navigation(
            startDestination = AppRoutes.MOVIES_LIST,
            route = AppRoutes.MOVIES_ROOT
        ) {
            composable(route = AppRoutes.MOVIES_LIST) {
                val startScreenViewModel: StartScreenViewModel = koinInject()
                val screenState by startScreenViewModel.state.collectAsState()

                LaunchedEffect(startScreenViewModel) {
                    startScreenViewModel.effect.collectLatest { screenEffect ->
                        when (screenEffect) {
                            is StartScreenEffect.OpenMovieDetails -> {
                                navController.navigate(
                                    AppRoutes.movieDetails(screenEffect.targetMovieId)
                                )
                            }

                            StartScreenEffect.OpenFilters -> {
                                navController.navigate(AppRoutes.FILTER_MOVIES)
                            }
                        }
                    }
                }

                StartScreen(
                    state = screenState,
                    onIntent = startScreenViewModel::onIntent,
                    onMovieClick = { selectedMovieId ->
                        startScreenViewModel.onIntent(
                            StartScreenIntent.OpenMovie(selectedMovieId)
                        )
                    },
                    onFilterClick = {
                        startScreenViewModel.onIntent(StartScreenIntent.OpenFilterScreen)
                    },
                    onProfileClick = {
                        navController.navigate(AppRoutes.PROFILE)
                    }
                )
            }

            composable(route = AppRoutes.FILTER_MOVIES) {
                val startScreenViewModel: StartScreenViewModel = koinInject()
                val screenState by startScreenViewModel.state.collectAsState()

                FilterScreen(
                    state = screenState,
                    onIntent = startScreenViewModel::onIntent,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(route = AppRoutes.PROFILE) {
            val profileViewModel: ProfileViewModel = koinInject()
            val profileState by profileViewModel.state.collectAsState()

            LaunchedEffect(profileViewModel) {
                profileViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        ProfileEffect.OpenAuth -> {
                            navController.navigate(AppRoutes.AUTH) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            }

            ProfileScreen(
                state = profileState,
                onIntent = profileViewModel::onIntent,
                onBackClick = {
                    navController.popBackStack()
                },
                onFavoritesClick = {
                    navController.navigate(AppRoutes.FAVORITES)
                },
                onWatchlistClick = {
                    navController.navigate(AppRoutes.WATCHLIST)
                },
                onQuizClick = {
                    navController.navigate(AppRoutes.QUIZ)
                }
            )
        }

        composable(route = AppRoutes.FAVORITES) {
            val collectionViewModel: MovieCollectionViewModel = koinInject()
            val collectionState by collectionViewModel.state.collectAsState()

            LaunchedEffect(collectionViewModel) {
                collectionViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is MovieCollectionEffect.OpenMovieDetails -> {
                            navController.navigate(
                                AppRoutes.movieDetails(effect.movieId)
                            )
                        }
                    }
                }
            }

            MovieCollectionScreen(
                type = MovieCollectionType.FAVORITES,
                state = collectionState,
                onIntent = collectionViewModel::onIntent,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppRoutes.WATCHLIST) {
            val collectionViewModel: MovieCollectionViewModel = koinInject()
            val collectionState by collectionViewModel.state.collectAsState()

            LaunchedEffect(collectionViewModel) {
                collectionViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is MovieCollectionEffect.OpenMovieDetails -> {
                            navController.navigate(
                                AppRoutes.movieDetails(effect.movieId)
                            )
                        }
                    }
                }
            }

            MovieCollectionScreen(
                type = MovieCollectionType.WATCHLIST,
                state = collectionState,
                onIntent = collectionViewModel::onIntent,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppRoutes.QUIZ) {
            val quizViewModel: QuizViewModel = koinInject()
            val quizState by quizViewModel.state.collectAsState()

            LaunchedEffect(quizViewModel) {
                quizViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        QuizEffect.GoBack -> {
                            navController.popBackStack()
                        }
                    }
                }
            }

            QuizScreen(
                state = quizState,
                onIntent = quizViewModel::onIntent,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppRoutes.MOVIE_DETAILS) {
            val selectedMovieId = AppRoutes.selectedMovieId

            if (selectedMovieId == null) {
                navController.popBackStack()
                return@composable
            }

            val detailsViewModel: DetailsViewModel = koinInject()
            val detailsScreenState by detailsViewModel.state.collectAsState()

            DetailScreen(
                movieId = selectedMovieId,
                state = detailsScreenState,
                effect = detailsViewModel.effect,
                onIntent = detailsViewModel::onIntent,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
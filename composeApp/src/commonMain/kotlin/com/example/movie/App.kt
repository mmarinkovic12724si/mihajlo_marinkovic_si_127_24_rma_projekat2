package com.example.movie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import com.example.movie.core.platform.PlatformBackHandler
import com.example.movie.core.profile.ui.ProfileEffect
import com.example.movie.core.profile.ui.ProfileIntent
import com.example.movie.core.profile.ui.ProfileScreen
import com.example.movie.core.profile.ui.ProfileViewModel
import com.example.movie.core.quiz.ui.QuizEffect
import com.example.movie.core.quiz.ui.QuizScreen
import com.example.movie.core.quiz.ui.QuizViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun App() {
    val backStack = remember {
        mutableStateListOf<AppScreen>(AppScreen.Auth)
    }

    val currentScreen = backStack.lastOrNull() ?: AppScreen.Auth

    fun navigateTo(screen: AppScreen) {
        backStack.add(screen)
    }

    fun replaceAll(screen: AppScreen) {
        backStack.clear()
        backStack.add(screen)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    PlatformBackHandler(
        enabled = backStack.size > 1
    ) {
        goBack()
    }

    when (currentScreen) {
        AppScreen.Auth -> {
            val authViewModel: AuthViewModel = koinInject()
            val authState by authViewModel.state.collectAsState()

            LaunchedEffect(authViewModel) {
                authViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        AuthEffect.OpenMovies -> {
                            replaceAll(AppScreen.Movies)
                        }
                    }
                }
            }

            AuthScreen(
                state = authState,
                onIntent = authViewModel::onIntent
            )
        }

        AppScreen.Movies -> {
            val startScreenViewModel: StartScreenViewModel = koinInject()
            val screenState by startScreenViewModel.state.collectAsState()

            LaunchedEffect(startScreenViewModel) {
                startScreenViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        StartScreenEffect.OpenFilters -> {
                            navigateTo(AppScreen.Filters)
                        }

                        is StartScreenEffect.OpenMovieDetails -> {
                            navigateTo(
                                AppScreen.MovieDetails(
                                    movieId = effect.targetMovieId
                                )
                            )
                        }
                    }
                }
            }

            StartScreen(
                state = screenState,
                onIntent = startScreenViewModel::onIntent,
                onMovieClick = { selectedMovieId ->
                    startScreenViewModel.onIntent(
                        StartScreenIntent.OpenMovie(
                            targetMovieId = selectedMovieId
                        )
                    )
                },
                onFilterClick = {
                    startScreenViewModel.onIntent(
                        StartScreenIntent.OpenFilterScreen
                    )
                },
                onProfileClick = {
                    navigateTo(AppScreen.Profile)
                }
            )
        }

        AppScreen.Filters -> {
            val startScreenViewModel: StartScreenViewModel = koinInject()
            val screenState by startScreenViewModel.state.collectAsState()

            FilterScreen(
                state = screenState,
                onIntent = startScreenViewModel::onIntent,
                onBackClick = {
                    goBack()
                }
            )
        }

        AppScreen.Profile -> {
            val profileViewModel: ProfileViewModel = koinInject()
            val profileState by profileViewModel.state.collectAsState()

            LaunchedEffect(currentScreen) {
                profileViewModel.onIntent(ProfileIntent.LoadProfile)
            }

            LaunchedEffect(profileViewModel) {
                profileViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        ProfileEffect.OpenAuth -> {
                            replaceAll(AppScreen.Auth)
                        }
                    }
                }
            }

            ProfileScreen(
                state = profileState,
                onIntent = profileViewModel::onIntent,
                onBackClick = {
                    goBack()
                },
                onFavoritesClick = {
                    navigateTo(AppScreen.Favorites)
                },
                onWatchlistClick = {
                    navigateTo(AppScreen.Watchlist)
                },
                onQuizClick = {
                    navigateTo(AppScreen.Quiz)
                }
            )
        }

        AppScreen.Favorites -> {
            val collectionViewModel: MovieCollectionViewModel = koinInject()
            val collectionState by collectionViewModel.state.collectAsState()

            LaunchedEffect(collectionViewModel) {
                collectionViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is MovieCollectionEffect.OpenMovieDetails -> {
                            navigateTo(
                                AppScreen.MovieDetails(
                                    movieId = effect.movieId
                                )
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
                    goBack()
                }
            )
        }

        AppScreen.Watchlist -> {
            val collectionViewModel: MovieCollectionViewModel = koinInject()
            val collectionState by collectionViewModel.state.collectAsState()

            LaunchedEffect(collectionViewModel) {
                collectionViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is MovieCollectionEffect.OpenMovieDetails -> {
                            navigateTo(
                                AppScreen.MovieDetails(
                                    movieId = effect.movieId
                                )
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
                    goBack()
                }
            )
        }

        AppScreen.Quiz -> {
            val quizViewModel: QuizViewModel = koinInject()
            val quizState by quizViewModel.state.collectAsState()

            LaunchedEffect(quizViewModel) {
                quizViewModel.effect.collectLatest { effect ->
                    when (effect) {
                        QuizEffect.GoBack -> {
                            goBack()
                        }
                    }
                }
            }

            QuizScreen(
                state = quizState,
                onIntent = quizViewModel::onIntent,
                onBackClick = {
                    goBack()
                }
            )
        }

        is AppScreen.MovieDetails -> {
            val detailsViewModel: DetailsViewModel = koinInject()
            val detailsState by detailsViewModel.state.collectAsState()

            DetailScreen(
                movieId = currentScreen.movieId,
                state = detailsState,
                effect = detailsViewModel.effect,
                onIntent = detailsViewModel::onIntent,
                onBackClick = {
                    goBack()
                }
            )
        }
    }
}

private sealed interface AppScreen {

    data object Auth : AppScreen

    data object Movies : AppScreen

    data object Filters : AppScreen

    data object Profile : AppScreen

    data object Favorites : AppScreen

    data object Watchlist : AppScreen

    data object Quiz : AppScreen

    data class MovieDetails(
        val movieId: String
    ) : AppScreen
}
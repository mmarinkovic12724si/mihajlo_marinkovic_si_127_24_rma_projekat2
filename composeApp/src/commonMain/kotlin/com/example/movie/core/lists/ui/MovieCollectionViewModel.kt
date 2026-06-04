package com.example.movie.core.lists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieCollectionViewModel(
    private val movieListsRepository: MovieListsRepository,
    private val getImageBaseService: GetImageBaseService
) : ViewModel() {

    private val _state = MutableStateFlow(MovieCollectionState())
    val state: StateFlow<MovieCollectionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MovieCollectionEffect>()
    val effect: SharedFlow<MovieCollectionEffect> = _effect.asSharedFlow()

    private var currentType: MovieCollectionType = MovieCollectionType.FAVORITES
    private var observeJob: Job? = null

    fun onIntent(intent: MovieCollectionIntent) {
        when (intent) {
            is MovieCollectionIntent.Load -> {
                currentType = intent.type
                loadCollection(intent.type)
            }

            MovieCollectionIntent.Retry -> {
                loadCollection(currentType)
            }

            is MovieCollectionIntent.OpenMovie -> {
                openMovie(intent.movieId)
            }

            is MovieCollectionIntent.RemoveMovie -> {
                removeMovie(intent.movieId)
            }

            MovieCollectionIntent.ClearError -> {
                _state.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun loadCollection(
        type: MovieCollectionType
    ) {
        observeJob?.cancel()

        val title = when (type) {
            MovieCollectionType.FAVORITES -> "Favorites"
            MovieCollectionType.WATCHLIST -> "Watchlist"
        }

        val emptyMessage = when (type) {
            MovieCollectionType.FAVORITES -> "No favorite movies yet."
            MovieCollectionType.WATCHLIST -> "No movies in watchlist yet."
        }

        _state.update {
            it.copy(
                type = type,
                title = title,
                emptyMessage = emptyMessage,
                isLoading = true,
                errorMessage = null
            )
        }

        observeJob = viewModelScope.launch {
            val imageHost = try {
                getImageBaseService()
            } catch (exception: Exception) {
                ""
            }

            _state.update {
                it.copy(
                    imageHost = imageHost
                )
            }

            val syncResult = when (type) {
                MovieCollectionType.FAVORITES -> movieListsRepository.syncFavorites()
                MovieCollectionType.WATCHLIST -> movieListsRepository.syncWatchlist()
            }

            syncResult.exceptionOrNull()?.message?.let { message ->
                _state.update {
                    it.copy(
                        errorMessage = message
                    )
                }
            }

            val moviesFlow = when (type) {
                MovieCollectionType.FAVORITES -> movieListsRepository.observeFavoriteMovies()
                MovieCollectionType.WATCHLIST -> movieListsRepository.observeWatchlistMovies()
            }

            moviesFlow.collect { movies ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        movies = movies
                    )
                }
            }
        }
    }

    private fun removeMovie(
        movieId: String
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    removingMovieId = movieId,
                    errorMessage = null
                )
            }

            val result = when (currentType) {
                MovieCollectionType.FAVORITES -> {
                    movieListsRepository.removeFavorite(movieId)
                }

                MovieCollectionType.WATCHLIST -> {
                    movieListsRepository.removeFromWatchlist(movieId)
                }
            }

            _state.update {
                it.copy(
                    removingMovieId = null,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun openMovie(
        movieId: String
    ) {
        viewModelScope.launch {
            _effect.emit(
                MovieCollectionEffect.OpenMovieDetails(
                    movieId = movieId
                )
            )
        }
    }
}
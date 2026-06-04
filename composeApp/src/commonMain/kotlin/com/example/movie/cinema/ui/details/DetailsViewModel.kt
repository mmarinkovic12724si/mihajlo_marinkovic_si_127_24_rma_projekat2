package com.example.movie.cinema.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.app.common.UiState
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.cinema.domain.service.GetMovieDetailsService
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getMovieDetailsService: GetMovieDetailsService,
    private val getImageBaseService: GetImageBaseService,
    private val movieListsRepository: MovieListsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state

    private val _effect = Channel<DetailsEffect>()
    val effect = _effect.receiveAsFlow()

    private var selectedMovieId: String? = null
    private var listStateJob: Job? = null

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.FetchDetails -> {
                selectedMovieId = intent.targetMovieId
                observeMovieListState(intent.targetMovieId)
                syncRemoteLists()
                fetchMovieDetails(intent.targetMovieId)
            }

            DetailsIntent.RetryLoad -> {
                selectedMovieId?.let { movieId ->
                    observeMovieListState(movieId)
                    syncRemoteLists()
                    fetchMovieDetails(movieId)
                }
            }

            DetailsIntent.OnBackClick -> {
                sendBackEffect()
            }

            is DetailsIntent.OnTrailerClick -> {
                sendTrailerEffect(intent.trailerKey)
            }

            DetailsIntent.ToggleFavorite -> {
                toggleFavorite()
            }

            DetailsIntent.ToggleWatchlist -> {
                toggleWatchlist()
            }

            DetailsIntent.ClearMessage -> {
                _state.value = _state.value.copy(
                    message = null
                )
            }
        }
    }

    private fun fetchMovieDetails(targetMovieId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                screenState = UiState.Loading,
                message = null
            )

            try {
                val resolvedImageHost = getImageBaseService()
                val detailsBundle = getMovieDetailsService(targetMovieId)

                _state.value = _state.value.copy(
                    screenState = UiState.Success(detailsBundle),
                    bundle = detailsBundle,
                    imageHost = resolvedImageHost
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    screenState = UiState.Error(
                        description = exception.message ?: "Failed to load movie details"
                    )
                )
            }
        }
    }

    private fun observeMovieListState(movieId: String) {
        listStateJob?.cancel()

        listStateJob = viewModelScope.launch {
            combine(
                movieListsRepository.observeIsFavorite(movieId),
                movieListsRepository.observeIsInWatchlist(movieId)
            ) { isFavorite, isInWatchlist ->
                isFavorite to isInWatchlist
            }.collect { result ->
                _state.value = _state.value.copy(
                    isFavorite = result.first,
                    isInWatchlist = result.second
                )
            }
        }
    }

    private fun syncRemoteLists() {
        viewModelScope.launch {
            movieListsRepository.syncFavorites()
            movieListsRepository.syncWatchlist()
        }
    }

    private fun toggleFavorite() {
        val movieId = selectedMovieId ?: return
        val isCurrentlyFavorite = _state.value.isFavorite

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isFavoriteLoading = true,
                message = null
            )

            val result = movieListsRepository.toggleFavorite(
                movieId = movieId,
                isCurrentlyFavorite = isCurrentlyFavorite
            )

            _state.value = _state.value.copy(
                isFavoriteLoading = false,
                message = result.exceptionOrNull()?.message
            )
        }
    }

    private fun toggleWatchlist() {
        val movieId = selectedMovieId ?: return
        val isCurrentlyInWatchlist = _state.value.isInWatchlist

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isWatchlistLoading = true,
                message = null
            )

            val result = movieListsRepository.toggleWatchlist(
                movieId = movieId,
                isCurrentlyInWatchlist = isCurrentlyInWatchlist
            )

            _state.value = _state.value.copy(
                isWatchlistLoading = false,
                message = result.exceptionOrNull()?.message
            )
        }
    }

    private fun sendBackEffect() {
        viewModelScope.launch {
            _effect.send(DetailsEffect.GoBack)
        }
    }

    private fun sendTrailerEffect(trailerKey: String) {
        viewModelScope.launch {
            _effect.send(
                DetailsEffect.LaunchTrailer(
                    trailerUrl = "https://www.youtube.com/watch?v=$trailerKey"
                )
            )
        }
    }
}
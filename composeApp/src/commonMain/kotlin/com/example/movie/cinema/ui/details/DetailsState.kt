package com.example.movie.cinema.ui.details

import com.example.movie.app.common.UiState
import com.example.movie.cinema.model.MovieDetails

data class DetailsState(
    val screenState: UiState<MovieDetails> = UiState.Idle,
    val bundle: MovieDetails? = null,
    val imageHost: String = "",

    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,

    val isFavoriteLoading: Boolean = false,
    val isWatchlistLoading: Boolean = false,

    val message: String? = null
)
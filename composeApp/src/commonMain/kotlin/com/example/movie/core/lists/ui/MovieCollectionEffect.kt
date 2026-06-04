package com.example.movie.core.lists.ui

sealed interface MovieCollectionEffect {

    data class OpenMovieDetails(
        val movieId: String
    ) : MovieCollectionEffect
}
package com.example.movie.core.lists.ui

sealed interface MovieCollectionIntent {

    data class Load(
        val type: MovieCollectionType
    ) : MovieCollectionIntent

    data object Retry : MovieCollectionIntent

    data class OpenMovie(
        val movieId: String
    ) : MovieCollectionIntent

    data class RemoveMovie(
        val movieId: String
    ) : MovieCollectionIntent

    data object ClearError : MovieCollectionIntent
}
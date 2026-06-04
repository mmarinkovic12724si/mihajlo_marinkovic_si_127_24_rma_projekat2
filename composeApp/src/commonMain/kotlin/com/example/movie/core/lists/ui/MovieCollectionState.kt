package com.example.movie.core.lists.ui

import com.example.movie.cinema.model.Movie

data class MovieCollectionState(
    val type: MovieCollectionType = MovieCollectionType.FAVORITES,
    val title: String = "Favorites",
    val emptyMessage: String = "No favorite movies yet.",
    val isLoading: Boolean = true,
    val imageHost: String = "",
    val movies: List<Movie> = emptyList(),
    val removingMovieId: String? = null,
    val errorMessage: String? = null
)
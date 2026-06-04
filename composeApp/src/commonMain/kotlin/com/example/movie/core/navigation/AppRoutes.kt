package com.example.movie.core.navigation

object AppRoutes {

    const val AUTH = "auth"

    const val MOVIES_ROOT = "movies_root"
    const val MOVIES_LIST = "movies_list"
    const val FILTER_MOVIES = "filter_movies"
    const val MOVIE_DETAILS = "movie_details"

    const val PROFILE = "profile"
    const val FAVORITES = "favorites"
    const val WATCHLIST = "watchlist"
    const val QUIZ = "quiz"

    var selectedMovieId: String? = null
        private set

    fun movieDetails(targetMovieId: String): String {
        selectedMovieId = targetMovieId
        return MOVIE_DETAILS
    }
}
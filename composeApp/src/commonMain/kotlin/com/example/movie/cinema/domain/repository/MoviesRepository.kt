package com.example.movie.cinema.domain.repository

import com.example.movie.app.common.BrowseFilters
import com.example.movie.cinema.model.Genre
import com.example.movie.cinema.model.Movie
import com.example.movie.cinema.model.MovieDetails
import com.example.movie.cinema.model.MoviesPage
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    suspend fun getMovies(
        pageNumber: Int = 1,
        itemsPerPage: Int = 30,
        selectedFilters: BrowseFilters = BrowseFilters(),
        sortField: String = "imdb_rating",
        sortDirection: String = "desc"
    ): MoviesPage

    fun observeMovies(
        pageNumber: Int = 1,
        itemsPerPage: Int = 30,
        selectedFilters: BrowseFilters = BrowseFilters(),
        sortField: String = "imdb_rating",
        sortDirection: String = "desc"
    ): Flow<MoviesPage>

    fun observeMoviesByIds(
        movieIds: List<String>,
        fallbackPage: MoviesPage
    ): Flow<MoviesPage>

    suspend fun getMovieDetails(targetMovieId: String): MovieDetails

    suspend fun getGenres(): List<Genre>

    suspend fun getImageBaseUrl(): String
}
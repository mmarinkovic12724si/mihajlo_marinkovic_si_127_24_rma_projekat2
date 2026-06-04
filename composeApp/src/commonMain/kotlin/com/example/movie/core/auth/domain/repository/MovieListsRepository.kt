package com.example.movie.core.auth.domain.repository

import com.example.movie.cinema.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieListsRepository {

    fun observeFavoriteMovies(): Flow<List<Movie>>

    fun observeWatchlistMovies(): Flow<List<Movie>>

    fun observeIsFavorite(
        movieId: String
    ): Flow<Boolean>

    fun observeIsInWatchlist(
        movieId: String
    ): Flow<Boolean>

    suspend fun syncFavorites(): Result<Unit>

    suspend fun syncWatchlist(): Result<Unit>

    suspend fun toggleFavorite(
        movieId: String,
        isCurrentlyFavorite: Boolean
    ): Result<Unit>

    suspend fun toggleWatchlist(
        movieId: String,
        isCurrentlyInWatchlist: Boolean
    ): Result<Unit>

    suspend fun removeFavorite(
        movieId: String
    ): Result<Unit>

    suspend fun removeFromWatchlist(
        movieId: String
    ): Result<Unit>
}
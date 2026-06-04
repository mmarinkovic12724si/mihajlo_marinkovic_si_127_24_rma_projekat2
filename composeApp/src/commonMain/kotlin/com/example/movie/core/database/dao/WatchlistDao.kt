package com.example.movie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.movie.core.database.entity.MovieEntity
import com.example.movie.core.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("""
        SELECT movies.* FROM movies
        INNER JOIN watchlist ON movies.id = watchlist.movieId
        ORDER BY watchlist.addedAtMillis DESC
    """)
    fun observeWatchlistMovies(): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun observeIsInWatchlist(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>

    @Upsert
    suspend fun addToWatchlist(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun removeFromWatchlist(movieId: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()
}
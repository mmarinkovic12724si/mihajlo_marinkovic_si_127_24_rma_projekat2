package com.example.movie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.movie.core.database.entity.FavoriteEntity
import com.example.movie.core.database.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("""
        SELECT movies.* FROM movies
        INNER JOIN favorites ON movies.id = favorites.movieId
        ORDER BY favorites.addedAtMillis DESC
    """)
    fun observeFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun observeIsFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoriteCount(): Flow<Int>

    @Upsert
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun removeFavorite(movieId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}
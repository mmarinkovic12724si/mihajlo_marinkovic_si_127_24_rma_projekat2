package com.example.movie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.movie.core.database.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY imdbRating DESC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun observeMovieById(movieId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE id IN (:movieIds)")
    suspend fun getMoviesByIds(movieIds: List<String>): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id IN (:movieIds)")
    fun observeMoviesByIds(movieIds: List<String>): Flow<List<MovieEntity>>

    @Query("""
        SELECT * FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:genreId IS NULL OR genresText LIKE '%' || :genrePattern || '%')
        AND (:minYear IS NULL OR year >= :minYear)
        AND (:maxYear IS NULL OR year <= :maxYear)
        AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
            CASE WHEN :sortField = 'imdb_rating' AND :sortDirection = 'desc' THEN imdbRating END DESC,
            CASE WHEN :sortField = 'imdb_rating' AND :sortDirection = 'asc' THEN imdbRating END ASC,
            CASE WHEN :sortField = 'year' AND :sortDirection = 'desc' THEN year END DESC,
            CASE WHEN :sortField = 'year' AND :sortDirection = 'asc' THEN year END ASC,
            CASE WHEN :sortField = 'title' AND :sortDirection = 'desc' THEN title END DESC,
            CASE WHEN :sortField = 'title' AND :sortDirection = 'asc' THEN title END ASC,
            CASE WHEN :sortField = 'popularity' AND :sortDirection = 'desc' THEN popularity END DESC,
            CASE WHEN :sortField = 'popularity' AND :sortDirection = 'asc' THEN popularity END ASC,
            imdbRating DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getCachedMoviesPage(
        query: String?,
        genreId: Int?,
        genrePattern: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
        sortField: String,
        sortDirection: String,
        limit: Int,
        offset: Int
    ): List<MovieEntity>

    @Query("""
        SELECT * FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:genreId IS NULL OR genresText LIKE '%' || :genrePattern || '%')
        AND (:minYear IS NULL OR year >= :minYear)
        AND (:maxYear IS NULL OR year <= :maxYear)
        AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
            CASE WHEN :sortField = 'imdb_rating' AND :sortDirection = 'desc' THEN imdbRating END DESC,
            CASE WHEN :sortField = 'imdb_rating' AND :sortDirection = 'asc' THEN imdbRating END ASC,
            CASE WHEN :sortField = 'year' AND :sortDirection = 'desc' THEN year END DESC,
            CASE WHEN :sortField = 'year' AND :sortDirection = 'asc' THEN year END ASC,
            CASE WHEN :sortField = 'title' AND :sortDirection = 'desc' THEN title END DESC,
            CASE WHEN :sortField = 'title' AND :sortDirection = 'asc' THEN title END ASC,
            CASE WHEN :sortField = 'popularity' AND :sortDirection = 'desc' THEN popularity END DESC,
            CASE WHEN :sortField = 'popularity' AND :sortDirection = 'asc' THEN popularity END ASC,
            imdbRating DESC
        LIMIT :limit OFFSET :offset
    """)
    fun observeCachedMoviesPage(
        query: String?,
        genreId: Int?,
        genrePattern: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
        sortField: String,
        sortDirection: String,
        limit: Int,
        offset: Int
    ): Flow<List<MovieEntity>>

    @Query("""
        SELECT COUNT(*) FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:genreId IS NULL OR genresText LIKE '%' || :genrePattern || '%')
        AND (:minYear IS NULL OR year >= :minYear)
        AND (:maxYear IS NULL OR year <= :maxYear)
        AND (:minRating IS NULL OR imdbRating >= :minRating)
    """)
    suspend fun countCachedMovies(
        query: String?,
        genreId: Int?,
        genrePattern: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?
    ): Int

    @Query("""
        SELECT COUNT(*) FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:genreId IS NULL OR genresText LIKE '%' || :genrePattern || '%')
        AND (:minYear IS NULL OR year >= :minYear)
        AND (:maxYear IS NULL OR year <= :maxYear)
        AND (:minRating IS NULL OR imdbRating >= :minRating)
    """)
    fun observeCachedMoviesCount(
        query: String?,
        genreId: Int?,
        genrePattern: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?
    ): Flow<Int>

    @Query("""
        SELECT * FROM movies
        WHERE posterUrl IS NOT NULL OR backdropUrl IS NOT NULL
        ORDER BY imdbRating DESC
        LIMIT :limit
    """)
    suspend fun getQuizPool(limit: Int): List<MovieEntity>

    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearMovies()
}
package com.example.movie.core.auth.data.repository

import com.example.movie.cinema.data.mapper.toDomainMovies
import com.example.movie.cinema.data.mapper.toEntity
import com.example.movie.cinema.model.Movie
import com.example.movie.core.auth.domain.repository.AuthRepository
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import com.example.movie.core.database.dao.FavoriteDao
import com.example.movie.core.database.dao.MovieDao
import com.example.movie.core.database.dao.WatchlistDao
import com.example.movie.core.database.entity.FavoriteEntity
import com.example.movie.core.database.entity.WatchlistEntity
import com.example.movie.core.network.ShowtimeApi
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class MovieListsRepositoryImpl(
    private val showtimeApi: ShowtimeApi,
    private val authRepository: AuthRepository,
    private val movieDao: MovieDao,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao
) : MovieListsRepository {

    override fun observeFavoriteMovies(): Flow<List<Movie>> {
        return favoriteDao.observeFavoriteMovies().map { movieEntities ->
            movieEntities.toDomainMovies()
        }
    }

    override fun observeWatchlistMovies(): Flow<List<Movie>> {
        return watchlistDao.observeWatchlistMovies().map { movieEntities ->
            movieEntities.toDomainMovies()
        }
    }

    override fun observeIsFavorite(
        movieId: String
    ): Flow<Boolean> {
        return favoriteDao.observeIsFavorite(movieId)
    }

    override fun observeIsInWatchlist(
        movieId: String
    ): Flow<Boolean> {
        return watchlistDao.observeIsInWatchlist(movieId)
    }

    override suspend fun syncFavorites(): Result<Unit> {
        return try {
            val authorization = getAuthorizationHeader()
                ?: return Result.failure(Exception("Sesija je istekla. Prijavi se ponovo."))

            val remoteFavorites = showtimeApi.getFavorites(
                authorization = authorization
            )

            val favoriteMovieIds = remoteFavorites.map { movieDto ->
                movieDto.imdbId
            }

            val movieEntities = remoteFavorites.map { movieDto ->
                val existingEntity = movieDao.getMovieById(movieDto.imdbId)

                movieDto.toEntity(
                    existingEntity = existingEntity
                )
            }

            movieDao.upsertMovies(movieEntities)

            favoriteDao.clearFavorites()

            favoriteMovieIds.forEach { movieId ->
                favoriteDao.addFavorite(
                    FavoriteEntity(
                        movieId = movieId,
                        addedAtMillis = currentTimeMillis()
                    )
                )
            }

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Ne mogu da sinhronizujem favorite filmove."
                )
            } else {
                Result.failure(Exception("Ne mogu da sinhronizujem favorite filmove."))
            }
        } catch (exception: Exception) {
            Result.failure(Exception("Ne mogu da sinhronizujem favorite filmove."))
        }
    }

    override suspend fun syncWatchlist(): Result<Unit> {
        return try {
            val authorization = getAuthorizationHeader()
                ?: return Result.failure(Exception("Sesija je istekla. Prijavi se ponovo."))

            val remoteWatchlist = showtimeApi.getWatchlist(
                authorization = authorization
            )

            val watchlistMovieIds = remoteWatchlist.map { movieDto ->
                movieDto.imdbId
            }

            val movieEntities = remoteWatchlist.map { movieDto ->
                val existingEntity = movieDao.getMovieById(movieDto.imdbId)

                movieDto.toEntity(
                    existingEntity = existingEntity
                )
            }

            movieDao.upsertMovies(movieEntities)

            watchlistDao.clearWatchlist()

            watchlistMovieIds.forEach { movieId ->
                watchlistDao.addToWatchlist(
                    WatchlistEntity(
                        movieId = movieId,
                        addedAtMillis = currentTimeMillis()
                    )
                )
            }

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Ne mogu da sinhronizujem watchlist."
                )
            } else {
                Result.failure(Exception("Ne mogu da sinhronizujem watchlist."))
            }
        } catch (exception: Exception) {
            Result.failure(Exception("Ne mogu da sinhronizujem watchlist."))
        }
    }

    override suspend fun toggleFavorite(
        movieId: String,
        isCurrentlyFavorite: Boolean
    ): Result<Unit> {
        val authorization = getAuthorizationHeader()
            ?: return Result.failure(Exception("Sesija je istekla. Prijavi se ponovo."))

        return if (isCurrentlyFavorite) {
            removeFavoriteOptimistically(
                authorization = authorization,
                movieId = movieId
            )
        } else {
            addFavoriteOptimistically(
                authorization = authorization,
                movieId = movieId
            )
        }
    }

    override suspend fun toggleWatchlist(
        movieId: String,
        isCurrentlyInWatchlist: Boolean
    ): Result<Unit> {
        val authorization = getAuthorizationHeader()
            ?: return Result.failure(Exception("Sesija je istekla. Prijavi se ponovo."))

        return if (isCurrentlyInWatchlist) {
            removeFromWatchlistOptimistically(
                authorization = authorization,
                movieId = movieId
            )
        } else {
            addToWatchlistOptimistically(
                authorization = authorization,
                movieId = movieId
            )
        }
    }

    override suspend fun removeFavorite(
        movieId: String
    ): Result<Unit> {
        return toggleFavorite(
            movieId = movieId,
            isCurrentlyFavorite = true
        )
    }

    override suspend fun removeFromWatchlist(
        movieId: String
    ): Result<Unit> {
        return toggleWatchlist(
            movieId = movieId,
            isCurrentlyInWatchlist = true
        )
    }

    private suspend fun addFavoriteOptimistically(
        authorization: String,
        movieId: String
    ): Result<Unit> {
        favoriteDao.addFavorite(
            FavoriteEntity(
                movieId = movieId,
                addedAtMillis = currentTimeMillis()
            )
        )

        return try {
            showtimeApi.addFavorite(
                authorization = authorization,
                movieId = movieId
            )

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            favoriteDao.removeFavorite(movieId)

            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Favorite nije sačuvan na serveru."
                )
            } else {
                Result.failure(Exception("Favorite nije sačuvan na serveru."))
            }
        } catch (exception: Exception) {
            favoriteDao.removeFavorite(movieId)
            Result.failure(Exception("Favorite nije sačuvan na serveru."))
        }
    }

    private suspend fun removeFavoriteOptimistically(
        authorization: String,
        movieId: String
    ): Result<Unit> {
        favoriteDao.removeFavorite(movieId)

        return try {
            showtimeApi.removeFavorite(
                authorization = authorization,
                movieId = movieId
            )

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    movieId = movieId,
                    addedAtMillis = currentTimeMillis()
                )
            )

            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Favorite nije uklonjen sa servera."
                )
            } else {
                Result.failure(Exception("Favorite nije uklonjen sa servera."))
            }
        } catch (exception: Exception) {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    movieId = movieId,
                    addedAtMillis = currentTimeMillis()
                )
            )

            Result.failure(Exception("Favorite nije uklonjen sa servera."))
        }
    }

    private suspend fun addToWatchlistOptimistically(
        authorization: String,
        movieId: String
    ): Result<Unit> {
        watchlistDao.addToWatchlist(
            WatchlistEntity(
                movieId = movieId,
                addedAtMillis = currentTimeMillis()
            )
        )

        return try {
            showtimeApi.addToWatchlist(
                authorization = authorization,
                movieId = movieId
            )

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            watchlistDao.removeFromWatchlist(movieId)

            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Watchlist nije sačuvan na serveru."
                )
            } else {
                Result.failure(Exception("Watchlist nije sačuvan na serveru."))
            }
        } catch (exception: Exception) {
            watchlistDao.removeFromWatchlist(movieId)
            Result.failure(Exception("Watchlist nije sačuvan na serveru."))
        }
    }

    private suspend fun removeFromWatchlistOptimistically(
        authorization: String,
        movieId: String
    ): Result<Unit> {
        watchlistDao.removeFromWatchlist(movieId)

        return try {
            showtimeApi.removeFromWatchlist(
                authorization = authorization,
                movieId = movieId
            )

            Result.success(Unit)
        } catch (exception: ClientRequestException) {
            watchlistDao.addToWatchlist(
                WatchlistEntity(
                    movieId = movieId,
                    addedAtMillis = currentTimeMillis()
                )
            )

            if (exception.response.status.value == 401) {
                handlePossibleExpiredSession(
                    fallbackMessage = "Watchlist nije uklonjen sa servera."
                )
            } else {
                Result.failure(Exception("Watchlist nije uklonjen sa servera."))
            }
        } catch (exception: Exception) {
            watchlistDao.addToWatchlist(
                WatchlistEntity(
                    movieId = movieId,
                    addedAtMillis = currentTimeMillis()
                )
            )

            Result.failure(Exception("Watchlist nije uklonjen sa servera."))
        }
    }

    private suspend fun handlePossibleExpiredSession(
        fallbackMessage: String
    ): Result<Unit> {
        println("MOVIE LISTS DEBUG: Protected list request returned 401. Checking /me before logout...")

        val currentUser = authRepository.getMe()

        return if (currentUser == null) {
            println("MOVIE LISTS DEBUG: /me also failed. Session is really expired.")
            Result.failure(Exception("Sesija je istekla. Prijavi se ponovo."))
        } else {
            println("MOVIE LISTS DEBUG: /me still works for ${currentUser.username}. Not logging out.")
            Result.failure(Exception(fallbackMessage))
        }
    }

    private suspend fun getAuthorizationHeader(): String? {
        val token = authRepository.tokenFlow.first()

        if (token.isNullOrBlank()) {
            return null
        }

        return "Bearer $token"
    }

    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}
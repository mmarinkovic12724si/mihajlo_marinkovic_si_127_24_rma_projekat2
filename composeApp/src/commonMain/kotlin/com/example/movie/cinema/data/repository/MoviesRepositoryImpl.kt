package com.example.movie.cinema.data.repository

import com.example.movie.app.common.BrowseFilters
import com.example.movie.cinema.data.dto.ConfigDto
import com.example.movie.cinema.data.dto.ImageItemDto
import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.cinema.data.dto.VideoDto
import com.example.movie.cinema.data.mapper.toCastDomainList
import com.example.movie.cinema.data.mapper.toDomain
import com.example.movie.cinema.data.mapper.toDomainMovies
import com.example.movie.cinema.data.mapper.toEntity
import com.example.movie.cinema.data.mapper.toGenreDomainList
import com.example.movie.cinema.domain.repository.MoviesRepository
import com.example.movie.cinema.model.Genre
import com.example.movie.cinema.model.MovieDetails
import com.example.movie.cinema.model.MoviesPage
import com.example.movie.core.database.dao.MovieDao
import com.example.movie.core.network.MoviesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.ceil

class MoviesRepositoryImpl(
    private val moviesApi: MoviesApi,
    private val movieDao: MovieDao
) : MoviesRepository {

    private var cachedImageHost: String? = null

    override suspend fun getMovies(
        pageNumber: Int,
        itemsPerPage: Int,
        selectedFilters: BrowseFilters,
        sortField: String,
        sortDirection: String
    ): MoviesPage {
        val query = selectedFilters.searchText
            .trim()
            .takeIf { it.isNotBlank() }

        val genrePattern = selectedFilters.pickedGenreId?.let { genreId ->
            "|$genreId|"
        }

        return try {
            val pageResponse = moviesApi.getMovies(
                pageNumber = pageNumber,
                itemsPerPage = itemsPerPage,
                searchText = query,
                pickedGenreId = selectedFilters.pickedGenreId,
                yearFrom = selectedFilters.yearFrom,
                yearTo = selectedFilters.yearTo,
                ratingFrom = selectedFilters.ratingFrom,
                sortField = sortField,
                sortDirection = sortDirection
            )

            val pageEntities = pageResponse.results.map { movieItemDto: MovieListItemDto ->
                val existingEntity = movieDao.getMovieById(movieItemDto.imdbId)

                movieItemDto.toEntity(
                    existingEntity = existingEntity
                )
            }

            movieDao.upsertMovies(pageEntities)

            val pageMovieIds = pageResponse.results.map { movieItemDto ->
                movieItemDto.imdbId
            }

            val localMoviesById = movieDao.getMoviesByIds(pageMovieIds)
                .associateBy { movieEntity ->
                    movieEntity.id
                }

            val orderedLocalMovies = pageMovieIds.mapNotNull { movieId ->
                localMoviesById[movieId]
            }

            MoviesPage(
                movieItems = orderedLocalMovies.toDomainMovies(),
                itemsCount = pageResponse.totalCount,
                pagesCount = pageResponse.pageCount,
                activePage = pageResponse.pageNumber,
                limitPerPage = pageResponse.itemsPerPage
            )
        } catch (exception: Exception) {
            val offset = (pageNumber - 1) * itemsPerPage

            val cachedMovies = movieDao.getCachedMoviesPage(
                query = query,
                genreId = selectedFilters.pickedGenreId,
                genrePattern = genrePattern,
                minYear = selectedFilters.yearFrom,
                maxYear = selectedFilters.yearTo,
                minRating = selectedFilters.ratingFrom,
                sortField = sortField,
                sortDirection = sortDirection,
                limit = itemsPerPage,
                offset = offset
            )

            val cachedCount = movieDao.countCachedMovies(
                query = query,
                genreId = selectedFilters.pickedGenreId,
                genrePattern = genrePattern,
                minYear = selectedFilters.yearFrom,
                maxYear = selectedFilters.yearTo,
                minRating = selectedFilters.ratingFrom
            )

            val cachedPageCount = if (cachedCount == 0) {
                0
            } else {
                ceil(cachedCount.toDouble() / itemsPerPage.toDouble()).toInt()
            }

            MoviesPage(
                movieItems = cachedMovies.toDomainMovies(),
                itemsCount = cachedCount,
                pagesCount = cachedPageCount,
                activePage = pageNumber,
                limitPerPage = itemsPerPage
            )
        }
    }

    override fun observeMovies(
        pageNumber: Int,
        itemsPerPage: Int,
        selectedFilters: BrowseFilters,
        sortField: String,
        sortDirection: String
    ): Flow<MoviesPage> {
        val query = selectedFilters.searchText
            .trim()
            .takeIf { it.isNotBlank() }

        val genrePattern = selectedFilters.pickedGenreId?.let { genreId ->
            "|$genreId|"
        }

        val offset = (pageNumber - 1) * itemsPerPage

        return combine(
            movieDao.observeCachedMoviesPage(
                query = query,
                genreId = selectedFilters.pickedGenreId,
                genrePattern = genrePattern,
                minYear = selectedFilters.yearFrom,
                maxYear = selectedFilters.yearTo,
                minRating = selectedFilters.ratingFrom,
                sortField = sortField,
                sortDirection = sortDirection,
                limit = itemsPerPage,
                offset = offset
            ),
            movieDao.observeCachedMoviesCount(
                query = query,
                genreId = selectedFilters.pickedGenreId,
                genrePattern = genrePattern,
                minYear = selectedFilters.yearFrom,
                maxYear = selectedFilters.yearTo,
                minRating = selectedFilters.ratingFrom
            )
        ) { cachedMovies, cachedCount ->

            val cachedPageCount = if (cachedCount == 0) {
                0
            } else {
                ceil(cachedCount.toDouble() / itemsPerPage.toDouble()).toInt()
            }

            MoviesPage(
                movieItems = cachedMovies.toDomainMovies(),
                itemsCount = cachedCount,
                pagesCount = cachedPageCount,
                activePage = pageNumber,
                limitPerPage = itemsPerPage
            )
        }
    }

    override fun observeMoviesByIds(
        movieIds: List<String>,
        fallbackPage: MoviesPage
    ): Flow<MoviesPage> {
        if (movieIds.isEmpty()) {
            return flowOf(
                fallbackPage.copy(
                    movieItems = emptyList()
                )
            )
        }

        return movieDao.observeMoviesByIds(movieIds)
            .map { localMovies ->

                val localMoviesById = localMovies.associateBy { movieEntity ->
                    movieEntity.id
                }

                val orderedMovies = movieIds.mapNotNull { movieId ->
                    localMoviesById[movieId]
                }

                fallbackPage.copy(
                    movieItems = orderedMovies.toDomainMovies()
                )
            }
    }

    override suspend fun getMovieDetails(targetMovieId: String): MovieDetails {
        val movieDetailsResponse = moviesApi.getMovieDetails(targetMovieId)

        val castResponse = moviesApi.getMovieCast(
            targetMovieId = targetMovieId,
            itemsPerPage = 10
        )

        val imageResponse = moviesApi.getMovieImages(
            targetMovieId = targetMovieId,
            imageType = "backdrop"
        )

        val videoItems = moviesApi.getMovieVideos(
            targetMovieId = targetMovieId,
            videoType = "Trailer"
        )

        val movieCast = castResponse.results
            .toCastDomainList()
            .take(10)

        val castText = movieCast.joinToString(separator = "|") { castMember ->
            castMember.fullName
        }

        val existingEntity = movieDao.getMovieById(targetMovieId)

        movieDao.upsertMovie(
            movieDetailsResponse.toEntity(
                castText = castText,
                existingEntity = existingEntity
            )
        )

        val selectedMovie = movieDao.getMovieById(targetMovieId)
            ?.toDomain()
            ?: movieDetailsResponse.toDomain()

        val movieGenres = movieDetailsResponse.genreItems.toGenreDomainList()

        val backdropImages = imageResponse.backdropItems
            .mapNotNull { backdropDto: ImageItemDto ->
                backdropDto.imagePath
            }
            .distinct()
            .take(3)

        val youtubeTrailerKey = videoItems
            .firstOrNull { videoItem: VideoDto ->
                videoItem.platformName.equals("YouTube", ignoreCase = true) &&
                        videoItem.videoType.equals("Trailer", ignoreCase = true)
            }
            ?.videoKey

        return MovieDetails(
            selectedMovie = selectedMovie,
            movieGenres = movieGenres,
            movieCast = movieCast,
            backdropImages = backdropImages,
            youtubeTrailerKey = youtubeTrailerKey
        )
    }

    override suspend fun getGenres(): List<Genre> {
        return moviesApi.getGenres().toGenreDomainList()
    }

    override suspend fun getImageBaseUrl(): String {
        cachedImageHost?.let { cachedValue ->
            return cachedValue
        }

        val configEntries = moviesApi.getConfig()

        val resolvedImageHost = configEntries
            .firstOrNull { configEntry: ConfigDto ->
                configEntry.configKey == "image_base_url"
            }
            ?.configValue
            ?: "https://image.tmdb.org/t/p/"

        cachedImageHost = resolvedImageHost

        return resolvedImageHost
    }
}
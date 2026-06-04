package com.example.movie.core.network

import com.example.movie.cinema.data.dto.ConfigDto
import com.example.movie.cinema.data.dto.GenreDto
import com.example.movie.cinema.data.dto.ImagesDto
import com.example.movie.cinema.data.dto.MovieDetailsDto
import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.cinema.data.dto.PaginatedResponseDto
import com.example.movie.cinema.data.dto.PersonDto
import com.example.movie.cinema.data.dto.VideoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MoviesApiImpl(
    private val httpClient: HttpClient
) : MoviesApi {

    private val baseUrl = "https://rma.finlab.rs"

    override suspend fun getMovies(
        pageNumber: Int,
        itemsPerPage: Int,
        searchText: String?,
        pickedGenreId: Int?,
        yearFrom: Int?,
        yearTo: Int?,
        ratingFrom: Double?,
        sortField: String,
        sortDirection: String
    ): PaginatedResponseDto<MovieListItemDto> {
        return httpClient.get("$baseUrl/movies") {
            parameter("page", pageNumber)
            parameter("page_size", itemsPerPage)

            if (!searchText.isNullOrBlank()) {
                parameter("query", searchText)
            }

            if (pickedGenreId != null) {
                parameter("genre_id", pickedGenreId)
            }

            if (yearFrom != null) {
                parameter("min_year", yearFrom)
            }

            if (yearTo != null) {
                parameter("max_year", yearTo)
            }

            if (ratingFrom != null) {
                parameter("min_rating", ratingFrom)
            }

            parameter("sort_by", sortField)
            parameter("sort_order", sortDirection)
        }.body()
    }

    override suspend fun getMovieDetails(
        targetMovieId: String
    ): MovieDetailsDto {
        return httpClient.get("$baseUrl/movies/$targetMovieId").body()
    }

    override suspend fun getMovieCast(
        targetMovieId: String,
        itemsPerPage: Int
    ): PaginatedResponseDto<PersonDto> {
        return httpClient.get("$baseUrl/movies/$targetMovieId/cast") {
            parameter("page_size", itemsPerPage)
        }.body()
    }

    override suspend fun getMovieImages(
        targetMovieId: String,
        imageType: String
    ): ImagesDto {
        return httpClient.get("$baseUrl/movies/$targetMovieId/images") {
            parameter("type", imageType)
        }.body()
    }

    override suspend fun getMovieVideos(
        targetMovieId: String,
        videoType: String
    ): List<VideoDto> {
        return httpClient.get("$baseUrl/movies/$targetMovieId/videos") {
            parameter("type", videoType)
        }.body()
    }

    override suspend fun getGenres(): List<GenreDto> {
        return httpClient.get("$baseUrl/genres").body()
    }

    override suspend fun getConfig(): List<ConfigDto> {
        return httpClient.get("$baseUrl/config").body()
    }
}
package com.example.movie.core.network

import com.example.movie.cinema.data.dto.ConfigDto
import com.example.movie.cinema.data.dto.GenreDto
import com.example.movie.cinema.data.dto.ImagesDto
import com.example.movie.cinema.data.dto.MovieDetailsDto
import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.cinema.data.dto.PaginatedResponseDto
import com.example.movie.cinema.data.dto.PersonDto
import com.example.movie.cinema.data.dto.VideoDto

interface MoviesApi {

    suspend fun getMovies(
        pageNumber: Int,
        itemsPerPage: Int,
        searchText: String?,
        pickedGenreId: Int?,
        yearFrom: Int?,
        yearTo: Int?,
        ratingFrom: Double?,
        sortField: String,
        sortDirection: String
    ): PaginatedResponseDto<MovieListItemDto>

    suspend fun getMovieDetails(
        targetMovieId: String
    ): MovieDetailsDto

    suspend fun getMovieCast(
        targetMovieId: String,
        itemsPerPage: Int = 10
    ): PaginatedResponseDto<PersonDto>

    suspend fun getMovieImages(
        targetMovieId: String,
        imageType: String = "backdrop"
    ): ImagesDto

    suspend fun getMovieVideos(
        targetMovieId: String,
        videoType: String = "Trailer"
    ): List<VideoDto>

    suspend fun getGenres(): List<GenreDto>

    suspend fun getConfig(): List<ConfigDto>
}
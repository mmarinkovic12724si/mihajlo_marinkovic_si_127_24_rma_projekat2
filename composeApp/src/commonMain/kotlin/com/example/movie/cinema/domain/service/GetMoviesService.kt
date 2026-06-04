package com.example.movie.cinema.domain.service

import com.example.movie.app.common.BrowseFilters
import com.example.movie.cinema.domain.repository.MoviesRepository
import com.example.movie.cinema.model.MoviesPage
import kotlinx.coroutines.flow.Flow

class GetMoviesService(
    private val moviesRepository: MoviesRepository
) {
    suspend operator fun invoke(
        pageNumber: Int,
        itemsPerPage: Int,
        selectedFilters: BrowseFilters,
        sortField: String,
        sortDirection: String
    ): MoviesPage {
        return moviesRepository.getMovies(
            pageNumber = pageNumber,
            itemsPerPage = itemsPerPage,
            selectedFilters = selectedFilters,
            sortField = sortField,
            sortDirection = sortDirection
        )
    }

    fun observe(
        pageNumber: Int,
        itemsPerPage: Int,
        selectedFilters: BrowseFilters,
        sortField: String,
        sortDirection: String
    ): Flow<MoviesPage> {
        return moviesRepository.observeMovies(
            pageNumber = pageNumber,
            itemsPerPage = itemsPerPage,
            selectedFilters = selectedFilters,
            sortField = sortField,
            sortDirection = sortDirection
        )
    }

    fun observeMoviesByIds(
        movieIds: List<String>,
        fallbackPage: MoviesPage
    ): Flow<MoviesPage> {
        return moviesRepository.observeMoviesByIds(
            movieIds = movieIds,
            fallbackPage = fallbackPage
        )
    }
}
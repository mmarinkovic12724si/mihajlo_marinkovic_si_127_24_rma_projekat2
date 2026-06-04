package com.example.movie.cinema.domain.service

import com.example.movie.cinema.domain.repository.MoviesRepository

class GetImageBaseService(
    private val moviesRepository: MoviesRepository
) {
    suspend operator fun invoke(): String {
        return moviesRepository.getImageBaseUrl()
    }
}
package com.example.movie.core.network

import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.core.auth.data.dto.AuthResponseDto
import com.example.movie.core.auth.data.dto.LoginRequestDto
import com.example.movie.core.auth.data.dto.SignupRequestDto
import com.example.movie.core.auth.data.dto.UserDto

interface ShowtimeApi {

    suspend fun signup(
        body: SignupRequestDto
    ): AuthResponseDto

    suspend fun login(
        body: LoginRequestDto
    ): AuthResponseDto

    suspend fun getMe(
        authorization: String
    ): UserDto

    suspend fun getFavorites(
        authorization: String
    ): List<MovieListItemDto>

    suspend fun addFavorite(
        authorization: String,
        movieId: String
    )

    suspend fun removeFavorite(
        authorization: String,
        movieId: String
    )

    suspend fun getWatchlist(
        authorization: String
    ): List<MovieListItemDto>

    suspend fun addToWatchlist(
        authorization: String,
        movieId: String
    )

    suspend fun removeFromWatchlist(
        authorization: String,
        movieId: String
    )
}
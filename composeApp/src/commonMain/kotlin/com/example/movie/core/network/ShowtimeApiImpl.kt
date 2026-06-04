package com.example.movie.core.network

import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.core.auth.data.dto.AuthResponseDto
import com.example.movie.core.auth.data.dto.LoginRequestDto
import com.example.movie.core.auth.data.dto.SignupRequestDto
import com.example.movie.core.auth.data.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ShowtimeApiImpl(
    private val httpClient: HttpClient
) : ShowtimeApi {

    private val baseUrl = "https://rma.finlab.rs"

    override suspend fun signup(
        body: SignupRequestDto
    ): AuthResponseDto {
        return httpClient.post("$baseUrl/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    override suspend fun login(
        body: LoginRequestDto
    ): AuthResponseDto {
        return httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    override suspend fun getMe(
        authorization: String
    ): UserDto {
        return httpClient.get("$baseUrl/me") {
            header("Authorization", authorization)
        }.body()
    }

    override suspend fun getFavorites(
        authorization: String
    ): List<MovieListItemDto> {
        return httpClient.get("$baseUrl/me/favorites") {
            header("Authorization", authorization)
        }.body()
    }

    override suspend fun addFavorite(
        authorization: String,
        movieId: String
    ) {
        httpClient.post("$baseUrl/me/favorites/$movieId") {
            header("Authorization", authorization)
        }
    }

    override suspend fun removeFavorite(
        authorization: String,
        movieId: String
    ) {
        httpClient.delete("$baseUrl/me/favorites/$movieId") {
            header("Authorization", authorization)
        }
    }

    override suspend fun getWatchlist(
        authorization: String
    ): List<MovieListItemDto> {
        return httpClient.get("$baseUrl/me/watchlist") {
            header("Authorization", authorization)
        }.body()
    }

    override suspend fun addToWatchlist(
        authorization: String,
        movieId: String
    ) {
        httpClient.post("$baseUrl/me/watchlist/$movieId") {
            header("Authorization", authorization)
        }
    }

    override suspend fun removeFromWatchlist(
        authorization: String,
        movieId: String
    ) {
        httpClient.delete("$baseUrl/me/watchlist/$movieId") {
            header("Authorization", authorization)
        }
    }
}
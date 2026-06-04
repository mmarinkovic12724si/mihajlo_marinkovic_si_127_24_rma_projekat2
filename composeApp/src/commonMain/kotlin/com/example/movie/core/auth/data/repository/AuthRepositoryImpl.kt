package com.example.movie.core.auth.data.repository

import com.example.movie.core.auth.TokenStorage
import com.example.movie.core.auth.data.dto.LoginRequestDto
import com.example.movie.core.auth.data.dto.SignupRequestDto
import com.example.movie.core.auth.data.mapper.toUser
import com.example.movie.core.auth.domain.model.AuthResult
import com.example.movie.core.auth.domain.model.User
import com.example.movie.core.auth.domain.repository.AuthRepository
import com.example.movie.core.database.dao.FavoriteDao
import com.example.movie.core.database.dao.WatchlistDao
import com.example.movie.core.network.ShowtimeApi
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class AuthRepositoryImpl(
    private val showtimeApi: ShowtimeApi,
    private val tokenStorage: TokenStorage,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao
) : AuthRepository {

    override val tokenFlow: Flow<String?> = tokenStorage.tokenFlow

    override suspend fun signup(
        fullName: String,
        username: String,
        password: String
    ): AuthResult {
        return try {
            val response = showtimeApi.signup(
                body = SignupRequestDto(
                    fullName = fullName,
                    username = username,
                    password = password
                )
            )

            tokenStorage.saveToken(response.accessToken)

            AuthResult.Success(
                user = response.user.toUser()
            )
        } catch (e: ClientRequestException) {
            AuthResult.Error(
                message = when (e.response.status.value) {
                    400 -> "Proveri podatke. Sva polja su obavezna, username mora imati bar 3 karaktera, a password bar 8."
                    409 -> "Username je zauzet. Probaj drugi username."
                    else -> "Registracija nije uspela. Kod greške: ${e.response.status.value}"
                }
            )
        } catch (e: IOException) {
            AuthResult.Error(
                message = "Nema interneta ili server nije dostupan."
            )
        } catch (e: Exception) {
            AuthResult.Error(
                message = "Neočekivana greška pri registraciji."
            )
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): AuthResult {
        return try {
            val response = showtimeApi.login(
                body = LoginRequestDto(
                    username = username,
                    password = password
                )
            )

            tokenStorage.saveToken(response.accessToken)

            AuthResult.Success(
                user = response.user.toUser()
            )
        } catch (e: ClientRequestException) {
            AuthResult.Error(
                message = when (e.response.status.value) {
                    401 -> "Pogrešan username ili password."
                    else -> "Login nije uspeo. Kod greške: ${e.response.status.value}"
                }
            )
        } catch (e: IOException) {
            AuthResult.Error(
                message = "Nema interneta ili server nije dostupan."
            )
        } catch (e: Exception) {
            AuthResult.Error(
                message = "Neočekivana greška pri prijavi."
            )
        }
    }

    override suspend fun getMe(): User? {
        val token = tokenStorage.tokenFlow.first()

        if (token.isNullOrBlank()) {
            println("AUTH DEBUG: GET ME STOPPED BECAUSE TOKEN IS NULL OR BLANK")
            return null
        }

        return try {
            val user = showtimeApi.getMe(
                authorization = "Bearer $token"
            ).toUser()
            user
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401) {
                println("AUTH DEBUG: GET ME GOT 401, LOGOUT STARTED")
                logout()
            }
            null
        } catch (e: IOException) {
            println("AUTH DEBUG: GET ME NETWORK ERROR = ${e.message}")
            null
        } catch (e: Exception) {
            println("AUTH DEBUG: GET ME UNKNOWN ERROR = ${e.message}")
            null
        }
    }

    override suspend fun logout() {
        tokenStorage.clearToken()
        favoriteDao.clearFavorites()
        watchlistDao.clearWatchlist()
    }
}
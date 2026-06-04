package com.example.movie.core.auth.domain.repository

import com.example.movie.core.auth.domain.model.AuthResult
import com.example.movie.core.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val tokenFlow: Flow<String?>

    suspend fun signup(
        fullName: String,
        username: String,
        password: String
    ): AuthResult

    suspend fun login(
        username: String,
        password: String
    ): AuthResult

    suspend fun getMe(): User?

    suspend fun logout()
}
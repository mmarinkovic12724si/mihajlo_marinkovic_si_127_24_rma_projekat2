package com.example.movie.core.auth.domain.model

sealed class AuthResult {
    data class Success(
        val user: com.example.movie.core.auth.domain.model.User
    ) : AuthResult()

    data class Error(
        val message: String
    ) : AuthResult()
}
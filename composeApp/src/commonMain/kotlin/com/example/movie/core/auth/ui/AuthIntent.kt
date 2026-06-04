package com.example.movie.core.auth.ui

sealed interface AuthIntent {

    data object SwitchToLogin : AuthIntent

    data object SwitchToSignup : AuthIntent

    data class FullNameChanged(
        val value: String
    ) : AuthIntent

    data class UsernameChanged(
        val value: String
    ) : AuthIntent

    data class PasswordChanged(
        val value: String
    ) : AuthIntent

    data object Submit : AuthIntent
}
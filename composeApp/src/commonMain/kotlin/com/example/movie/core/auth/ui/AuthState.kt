package com.example.movie.core.auth.ui

data class AuthState(
    val mode: AuthMode = AuthMode.LOGIN,

    val fullName: String = "",
    val username: String = "",
    val password: String = "",

    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,

    val errorMessage: String? = null
)
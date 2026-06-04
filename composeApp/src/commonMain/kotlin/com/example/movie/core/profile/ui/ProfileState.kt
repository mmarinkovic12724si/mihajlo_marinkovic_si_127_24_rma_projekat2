package com.example.movie.core.profile.ui

data class ProfileState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val username: String = "",
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestScore: Double = 0.0,
    val gamesPlayed: Int = 0,
    val errorMessage: String? = null
)
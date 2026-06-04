package com.example.movie.core.auth.ui

sealed interface AuthEffect {

    data object OpenMovies : AuthEffect
}
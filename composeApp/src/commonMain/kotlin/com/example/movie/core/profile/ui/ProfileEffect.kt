package com.example.movie.core.profile.ui

sealed interface ProfileEffect {

    data object OpenAuth : ProfileEffect
}
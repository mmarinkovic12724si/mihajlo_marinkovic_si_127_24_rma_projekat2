package com.example.movie.core.profile.ui

sealed interface ProfileIntent {

    data object LoadProfile : ProfileIntent

    data object Logout : ProfileIntent
}
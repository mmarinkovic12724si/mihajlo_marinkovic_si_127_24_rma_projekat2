package com.example.movie.core.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequestDto(
    @SerialName("full_name")
    val fullName: String,

    val username: String,
    val password: String
)
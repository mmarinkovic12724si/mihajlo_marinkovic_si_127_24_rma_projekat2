package com.example.movie.core.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val username: String,

    @SerialName("full_name")
    val fullName: String
)
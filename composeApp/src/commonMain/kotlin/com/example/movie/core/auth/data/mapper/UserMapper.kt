package com.example.movie.core.auth.data.mapper

import com.example.movie.core.auth.data.dto.UserDto
import com.example.movie.core.auth.domain.model.User

fun UserDto.toUser(): User {
    return User(
        id = id,
        username = username,
        fullName = fullName
    )
}
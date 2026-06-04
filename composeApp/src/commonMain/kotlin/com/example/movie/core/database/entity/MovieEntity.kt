package com.example.movie.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val overview: String,

    val year: Int?,
    val runtime: Int?,

    val imdbRating: Double,
    val imdbVotes: Int,
    val popularity: Double,

    val posterUrl: String?,
    val backdropUrl: String?,
    val genresText: String,
    val castText: String,

    val languageCode: String?,
    val productionBudget: Long?,
    val boxOfficeRevenue: Long?,
    val tmdbScore: Double?,

    val updatedAtMillis: Long
)
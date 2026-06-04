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

    /*
        Čuvamo id-jeve žanrova kao tekst, npr:
        |28|12|878|

        Tako možemo kroz SQL da proverimo da li film ima žanr:
        genresText LIKE '%|28|%'
    */
    val genresText: String,

    /*
        Kasnije za kviz "Guess Lead Actor".
        Za sada može biti prazan tekst, a kad otvorimo details upisaćemo cast.
    */
    val castText: String,

    val languageCode: String?,
    val productionBudget: Long?,
    val boxOfficeRevenue: Long?,
    val tmdbScore: Double?,

    val updatedAtMillis: Long
)
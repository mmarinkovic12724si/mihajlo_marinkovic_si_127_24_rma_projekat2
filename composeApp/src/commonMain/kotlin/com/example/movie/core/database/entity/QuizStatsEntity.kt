package com.example.movie.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey
    val id: Int = 1,

    val bestScore: Double,
    val gamesPlayed: Int
)
package com.example.movie.core.quiz.domain.model

data class QuizResult(
    val score: Double,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val usedSeconds: Int
)
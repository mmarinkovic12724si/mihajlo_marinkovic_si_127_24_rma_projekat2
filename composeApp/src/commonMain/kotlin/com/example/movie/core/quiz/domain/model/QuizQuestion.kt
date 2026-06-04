package com.example.movie.core.quiz.domain.model

data class QuizQuestion(
    val id: Int,
    val type: QuizQuestionType,
    val title: String,
    val subtitle: String,
    val movieId: String,
    val movieTitle: String,
    val imagePath: String?,
    val options: List<String>,
    val correctAnswer: String,
    val categoryId: Int = 1
)
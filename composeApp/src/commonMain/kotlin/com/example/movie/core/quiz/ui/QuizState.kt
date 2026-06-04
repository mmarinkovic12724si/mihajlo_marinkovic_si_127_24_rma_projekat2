package com.example.movie.core.quiz.ui

import com.example.movie.core.quiz.domain.model.QuizQuestion
import com.example.movie.core.quiz.domain.model.QuizResult

data class QuizState(
    val isLoading: Boolean = true,
    val imageHost: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val correctAnswers: Int = 0,
    val timeLeftSeconds: Int = 60,
    val isFinished: Boolean = false,
    val result: QuizResult? = null,
    val errorMessage: String? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val questionNumber: Int
        get() = currentQuestionIndex + 1

    val totalQuestions: Int
        get() = questions.size
}
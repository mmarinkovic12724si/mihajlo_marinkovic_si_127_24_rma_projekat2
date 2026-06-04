package com.example.movie.core.quiz.ui

sealed interface QuizIntent {

    data object StartQuiz : QuizIntent

    data class SelectAnswer(
        val answer: String
    ) : QuizIntent

    data object Retry : QuizIntent

    data object ExitQuiz : QuizIntent
}
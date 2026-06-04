package com.example.movie.core.quiz.ui

sealed interface QuizEffect {

    data object GoBack : QuizEffect
}
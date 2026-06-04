package com.example.movie.core.quiz.domain.repository

import com.example.movie.core.quiz.domain.model.QuizQuestion
import com.example.movie.core.quiz.domain.model.QuizResult

interface QuizRepository {

    suspend fun generateQuizSession(): Result<List<QuizQuestion>>

    suspend fun saveQuizResult(
        result: QuizResult
    )
}
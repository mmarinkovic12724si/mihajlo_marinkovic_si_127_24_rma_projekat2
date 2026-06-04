package com.example.movie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.movie.core.database.entity.QuizStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizStatsDao {

    @Query("SELECT * FROM quiz_stats WHERE id = 1 LIMIT 1")
    fun observeQuizStats(): Flow<QuizStatsEntity?>

    @Query("SELECT * FROM quiz_stats WHERE id = 1 LIMIT 1")
    suspend fun getQuizStats(): QuizStatsEntity?

    @Upsert
    suspend fun upsertQuizStats(stats: QuizStatsEntity)

    @Query("DELETE FROM quiz_stats")
    suspend fun clearQuizStats()
}
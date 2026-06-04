@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.example.movie.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.movie.core.database.dao.FavoriteDao
import com.example.movie.core.database.dao.MovieDao
import com.example.movie.core.database.dao.QuizStatsDao
import com.example.movie.core.database.dao.WatchlistDao
import com.example.movie.core.database.entity.FavoriteEntity
import com.example.movie.core.database.entity.MovieEntity
import com.example.movie.core.database.entity.QuizStatsEntity
import com.example.movie.core.database.entity.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizStatsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun watchlistDao(): WatchlistDao

    abstract fun quizStatsDao(): QuizStatsDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
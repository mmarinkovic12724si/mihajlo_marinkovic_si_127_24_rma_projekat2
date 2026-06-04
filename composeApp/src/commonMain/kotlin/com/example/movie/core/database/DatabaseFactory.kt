package com.example.movie.core.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}
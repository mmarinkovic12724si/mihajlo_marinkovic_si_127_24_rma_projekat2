package com.example.movie.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)

        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = databaseFile.absolutePath
        )
    }
}
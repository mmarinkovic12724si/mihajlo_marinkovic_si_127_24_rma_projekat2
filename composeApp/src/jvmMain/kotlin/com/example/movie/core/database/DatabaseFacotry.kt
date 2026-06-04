package com.example.movie.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatabaseFactory {

    actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val databaseFile = File(
            System.getProperty("java.io.tmpdir"),
            DATABASE_NAME
        )

        return Room.databaseBuilder<AppDatabase>(
            name = databaseFile.absolutePath
        )
    }
}
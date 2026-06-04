package com.example.movie.core.di

import com.example.movie.core.auth.TokenDataStoreFactory
import com.example.movie.core.auth.TokenStorage
import com.example.movie.core.database.AppDatabase
import com.example.movie.core.database.DatabaseFactory
import com.example.movie.core.database.createAppDatabase
import org.koin.dsl.module

val databaseModule = module {

    single {
        DatabaseFactory()
    }

    single {
        createAppDatabase(
            builder = get<DatabaseFactory>().createDatabaseBuilder()
        )
    }

    single {
        get<AppDatabase>().movieDao()
    }

    single {
        get<AppDatabase>().favoriteDao()
    }

    single {
        get<AppDatabase>().watchlistDao()
    }

    single {
        get<AppDatabase>().quizStatsDao()
    }

    single {
        TokenDataStoreFactory()
    }

    single {
        get<TokenDataStoreFactory>().createDataStore()
    }

    single {
        TokenStorage(
            dataStore = get()
        )
    }
}
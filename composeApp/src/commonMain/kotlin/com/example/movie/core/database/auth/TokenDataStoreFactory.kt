package com.example.movie.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class TokenDataStoreFactory {
    fun createDataStore(): DataStore<Preferences>
}
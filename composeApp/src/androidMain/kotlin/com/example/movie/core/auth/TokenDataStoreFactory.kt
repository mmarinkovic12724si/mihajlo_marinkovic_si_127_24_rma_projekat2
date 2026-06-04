package com.example.movie.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual class TokenDataStoreFactory(
    private val context: Context
) {
    actual fun createDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir
                    .resolve(TOKEN_DATASTORE_FILE_NAME)
                    .absolutePath
                    .toPath()
            }
        )
    }
}
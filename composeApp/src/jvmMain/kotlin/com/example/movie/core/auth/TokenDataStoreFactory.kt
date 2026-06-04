package com.example.movie.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual class TokenDataStoreFactory {
    actual fun createDataStore(): DataStore<Preferences> {
        val file = File(
            System.getProperty("java.io.tmpdir"),
            TOKEN_DATASTORE_FILE_NAME
        )

        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                file.absolutePath.toPath()
            }
        )
    }
}
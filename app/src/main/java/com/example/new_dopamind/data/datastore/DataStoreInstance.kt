package com.example.new_dopamind.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DataStoreInstance {
    private const val DATA_STORE_NAME = "user_preference"
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(DATA_STORE_NAME)

    fun getInstance(context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
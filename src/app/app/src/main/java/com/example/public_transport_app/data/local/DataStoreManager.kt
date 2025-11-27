package com.example.public_transport_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.public_transport_app.data.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    suspend fun saveKeyValue(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    suspend fun removeKey(key: String) {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit { settings ->
            settings.remove(dataStoreKey)
        }
    }

    fun getValueFlow(key: String, default: String? = ""): Flow<String?> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[dataStoreKey] ?: default
        }
    }

    suspend fun saveKeyValues(data: Map<String, String>) {
        context.dataStore.edit { settings ->
            data.forEach { (key, value) ->
                val dataStoreKey = stringPreferencesKey(key)
                settings[dataStoreKey] = value
            }
        }
    }

    suspend fun removeKeys(keys: List<String>) {
        context.dataStore.edit { settings ->
            keys.forEach { key ->
                val dataStoreKey = stringPreferencesKey(key)
                settings.remove(dataStoreKey)
            }
        }
    }

}

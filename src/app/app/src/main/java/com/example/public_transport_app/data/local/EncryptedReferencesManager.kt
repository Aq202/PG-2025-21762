package com.example.public_transport_app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
    }

    private val sharedPrefs: SharedPreferences = createEncryptedSharedPreferences(context)

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveKeyValue(key: String, value: String) {
        sharedPrefs.edit { putString(key, value) }
    }

    fun getValue(key: String, default: String = ""): String {
        return sharedPrefs.getString(key, default) ?: default
    }

    fun removeKey(key: String) {
        sharedPrefs.edit { remove(key) }
    }
}

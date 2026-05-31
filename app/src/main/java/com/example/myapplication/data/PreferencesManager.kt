package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Device-local UI preferences only (theme, language, notification toggle).
 * NO user data — all personal info / boarding passes live on the backend.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "mypass_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language") // "en", "fr", "ar"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val isDarkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[DARK_MODE] ?: false }
    val language: Flow<String> = context.settingsDataStore.data.map { it[LANGUAGE] ?: "en" }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val biometricEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.settingsDataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setBiometric(enabled: Boolean) {
        context.settingsDataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }
}

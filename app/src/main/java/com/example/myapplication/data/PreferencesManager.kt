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

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "mypass_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")  // "en", "fr", "ar"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        // Personal info saved locally
        val SAVED_FULL_NAME = stringPreferencesKey("saved_full_name")
        val SAVED_EMAIL = stringPreferencesKey("saved_email")
        val SAVED_PHONE = stringPreferencesKey("saved_phone")
        val SAVED_PASSPORT_NUMBER = stringPreferencesKey("saved_passport_number")
        val SAVED_NATIONALITY = stringPreferencesKey("saved_nationality")
        val SAVED_DATE_OF_BIRTH = stringPreferencesKey("saved_date_of_birth")
    }

    val isDarkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[DARK_MODE] ?: false }
    val language: Flow<String> = context.settingsDataStore.data.map { it[LANGUAGE] ?: "en" }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val biometricEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }

    val savedFullName: Flow<String> = context.settingsDataStore.data.map { it[SAVED_FULL_NAME] ?: "" }
    val savedEmail: Flow<String> = context.settingsDataStore.data.map { it[SAVED_EMAIL] ?: "" }
    val savedPhone: Flow<String> = context.settingsDataStore.data.map { it[SAVED_PHONE] ?: "" }
    val savedPassportNumber: Flow<String> = context.settingsDataStore.data.map { it[SAVED_PASSPORT_NUMBER] ?: "" }
    val savedNationality: Flow<String> = context.settingsDataStore.data.map { it[SAVED_NATIONALITY] ?: "" }
    val savedDateOfBirth: Flow<String> = context.settingsDataStore.data.map { it[SAVED_DATE_OF_BIRTH] ?: "" }

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

    suspend fun savePersonalInfo(
        fullName: String,
        email: String,
        phone: String,
        passportNumber: String,
        nationality: String,
        dateOfBirth: String
    ) {
        context.settingsDataStore.edit {
            it[SAVED_FULL_NAME] = fullName
            it[SAVED_EMAIL] = email
            it[SAVED_PHONE] = phone
            it[SAVED_PASSPORT_NUMBER] = passportNumber
            it[SAVED_NATIONALITY] = nationality
            it[SAVED_DATE_OF_BIRTH] = dateOfBirth
        }
    }
}

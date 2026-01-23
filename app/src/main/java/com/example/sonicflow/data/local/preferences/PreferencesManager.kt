package com.example.sonicflow.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Extension pour créer le DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonicflow_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    // Clés des préférences
    private object PreferencesKeys {
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val CURRENT_TRACK_ID = longPreferencesKey("current_track_id")
        val CURRENT_POSITION = longPreferencesKey("current_position")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    suspend fun saveRepeatMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] = mode
        }
    }

    val repeatMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] ?: "OFF"
        }

    suspend fun saveShuffleEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_ENABLED] = enabled
        }
    }

    val shuffleEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SHUFFLE_ENABLED] ?: false
        }

    suspend fun saveCurrentTrackId(trackId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_TRACK_ID] = trackId
        }
    }

    val currentTrackId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_TRACK_ID]
        }

    suspend fun saveCurrentPosition(position: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_POSITION] = position
        }
    }

    val currentPosition: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_POSITION] ?: 0L
        }

    suspend fun savePlaybackSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_SPEED] = speed
        }
    }

    val playbackSpeed: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f
        }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
        }

    suspend fun saveSortOrder(order: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = order
        }
    }

    val sortOrder: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] ?: "TITLE_ASC"
        }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
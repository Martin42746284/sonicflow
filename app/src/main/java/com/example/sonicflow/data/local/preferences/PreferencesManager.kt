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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonicflow_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    // Keys pour les préférences
    private object PreferencesKeys {
        val LAST_PLAYED_TRACK_ID = longPreferencesKey("last_played_track_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        val LAST_PLAYED_PLAYLIST_ID = longPreferencesKey("last_played_playlist_id")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val REPEAT_MODE = intPreferencesKey("repeat_mode") // 0: Off, 1: One, 2: All
        val SORT_ORDER = stringPreferencesKey("sort_order") // "title", "artist", "duration", "date_added"
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    // Sauvegarder l'état de lecture
    suspend fun savePlaybackState(trackId: Long, position: Long, playlistId: Long? = null) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_TRACK_ID] = trackId
            preferences[PreferencesKeys.LAST_PLAYED_POSITION] = position
            playlistId?.let {
                preferences[PreferencesKeys.LAST_PLAYED_PLAYLIST_ID] = it
            }
        }
    }

    // Récupérer l'état de lecture
    val playbackState: Flow<PlaybackState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            PlaybackState(
                lastPlayedTrackId = preferences[PreferencesKeys.LAST_PLAYED_TRACK_ID] ?: -1L,
                lastPlayedPosition = preferences[PreferencesKeys.LAST_PLAYED_POSITION] ?: 0L,
                lastPlayedPlaylistId = preferences[PreferencesKeys.LAST_PLAYED_PLAYLIST_ID]
            )
        }

    // Shuffle mode
    suspend fun setShuffleMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_MODE] = enabled
        }
    }

    val shuffleMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHUFFLE_MODE] ?: false
        }

    // Repeat mode
    suspend fun setRepeatMode(mode: RepeatMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] = mode.value
        }
    }

    val repeatMode: Flow<RepeatMode> = dataStore.data
        .map { preferences ->
            val mode = preferences[PreferencesKeys.REPEAT_MODE] ?: 0
            RepeatMode.fromValue(mode)
        }

    // Sort order
    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = order.name
        }
    }

    val sortOrder: Flow<SortOrder> = dataStore.data
        .map { preferences ->
            val order = preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.TITLE.name
            SortOrder.valueOf(order)
        }

    // Theme mode
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            val mode = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(mode)
        }

    // First launch
    suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirst
        }
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
        }

    // Clear all preferences
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Data classes et enums
data class PlaybackState(
    val lastPlayedTrackId: Long,
    val lastPlayedPosition: Long,
    val lastPlayedPlaylistId: Long? = null
)

enum class RepeatMode(val value: Int) {
    OFF(0),
    ONE(1),
    ALL(2);

    companion object {
        fun fromValue(value: Int): RepeatMode {
            return entries.find { it.value == value } ?: OFF
        }
    }
}

enum class SortOrder {
    TITLE,
    ARTIST,
    DURATION,
    DATE_ADDED
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

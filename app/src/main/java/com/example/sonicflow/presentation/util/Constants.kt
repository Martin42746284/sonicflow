package com.example.sonicflow.presentation.util

/**
 * Constantes utilisées dans l'application
 */
object Constants {

    // Navigation
    object Routes {
        const val SIGN_IN = "sign_in"
        const val SIGN_UP = "sign_up"
        const val LIBRARY = "library"
        const val PLAYER = "player"
        const val PLAYLISTS = "playlists"
        const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"

        fun playlistDetail(playlistId: Long) = "playlist_detail/$playlistId"
    }

    // Database
    object Database {
        const val NAME = "sonicflow_database"
        const val VERSION = 1
    }

    // Preferences
    object Preferences {
        const val NAME = "sonicflow_preferences"
        const val FIRST_LAUNCH = "first_launch"
        const val THEME_MODE = "theme_mode"
    }

    // Waveform
    object Waveform {
        const val DEFAULT_SAMPLES_COUNT = 100
        const val MIN_SAMPLES_COUNT = 50
        const val MAX_SAMPLES_COUNT = 200
    }

    // Player
    object Player {
        const val SEEK_BACK_THRESHOLD_MS = 3000L
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "sonicflow_playback"
        const val NOTIFICATION_CHANNEL_NAME = "Music Playback"
    }

    // UI
    object UI {
        const val ANIMATION_DURATION_SHORT = 150
        const val ANIMATION_DURATION_MEDIUM = 300
        const val ANIMATION_DURATION_LONG = 500

        const val DEBOUNCE_DELAY = 300L
        const val SEARCH_DELAY = 500L
    }

    // Validation
    object Validation {
        const val PLAYLIST_NAME_MAX_LENGTH = 100
        const val PLAYLIST_DESCRIPTION_MAX_LENGTH = 500
        const val MIN_PASSWORD_LENGTH = 6
    }

    // Media
    object Media {
        const val SUPPORTED_AUDIO_FORMATS = "*.mp3,*.m4a,*.wav,*.flac,*.ogg,*.aac"
    }
}

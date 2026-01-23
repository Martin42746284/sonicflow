package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.preferences.PreferencesManager
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du PlayerRepository
 * Note : L'intégration avec ExoPlayer sera faite plus tard
 */
@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : PlayerRepository {

    // États temporaires (seront remplacés par ExoPlayer)
    private val _currentTrack = MutableStateFlow<Track?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _queue = MutableStateFlow<List<Track>>(emptyList())

    override suspend fun playTrack(track: Track) {
        _currentTrack.value = track
        _isPlaying.value = true
        _duration.value = track.duration
        _currentPosition.value = 0L

        // Sauvegarder dans les préférences
        preferencesManager.saveCurrentTrackId(track.id)
        preferencesManager.saveCurrentPosition(0L)
    }

    override suspend fun pauseTrack() {
        _isPlaying.value = false
        // Sauvegarder la position actuelle
        preferencesManager.saveCurrentPosition(_currentPosition.value)
    }

    override suspend fun resumeTrack() {
        _isPlaying.value = true
    }

    override suspend fun skipToNext() {
        val currentQueue = _queue.value
        val currentTrack = _currentTrack.value

        if (currentTrack != null && currentQueue.isNotEmpty()) {
            val currentIndex = currentQueue.indexOf(currentTrack)
            val nextIndex = currentIndex + 1

            if (nextIndex < currentQueue.size) {
                playTrack(currentQueue[nextIndex])
            }
        }
    }

    override suspend fun skipToPrevious() {
        val currentQueue = _queue.value
        val currentTrack = _currentTrack.value

        if (currentTrack != null && currentQueue.isNotEmpty()) {
            val currentIndex = currentQueue.indexOf(currentTrack)
            val previousIndex = currentIndex - 1

            if (previousIndex >= 0) {
                playTrack(currentQueue[previousIndex])
            }
        }
    }

    override suspend fun seekTo(position: Long) {
        _currentPosition.value = position
        preferencesManager.saveCurrentPosition(position)
    }

    override fun getCurrentTrack(): Flow<Track?> {
        return _currentTrack.asStateFlow()
    }

    override fun isPlaying(): Flow<Boolean> {
        return _isPlaying.asStateFlow()
    }

    override fun getCurrentPosition(): Flow<Long> {
        return _currentPosition.asStateFlow()
    }

    override fun getDuration(): Flow<Long> {
        return _duration.asStateFlow()
    }

    override fun getRepeatMode(): Flow<RepeatMode> {
        return preferencesManager.repeatMode.map { mode ->
            try {
                RepeatMode.valueOf(mode)
            } catch (e: IllegalArgumentException) {
                RepeatMode.OFF
            }
        }
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        preferencesManager.saveRepeatMode(mode.name)
    }

    override suspend fun toggleRepeatMode() {
        val currentMode = preferencesManager.repeatMode.first()
        val newMode = when (currentMode) {
            "OFF" -> RepeatMode.ALL
            "ALL" -> RepeatMode.ONE
            "ONE" -> RepeatMode.OFF
            else -> RepeatMode.OFF
        }
        setRepeatMode(newMode)
    }

    override fun getShuffleEnabled(): Flow<Boolean> {
        return preferencesManager.shuffleEnabled
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        preferencesManager.saveShuffleEnabled(enabled)
    }

    override suspend fun toggleShuffleMode() {
        val currentState = preferencesManager.shuffleEnabled.first()
        setShuffleEnabled(!currentState)
    }

    override fun getQueue(): Flow<List<Track>> {
        return _queue.asStateFlow()
    }

    override suspend fun setQueue(tracks: List<Track>) {
        _queue.value = tracks
    }

    override suspend fun release() {
        _currentTrack.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _queue.value = emptyList()
    }

    // Helper pour first()
    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        this.collect {
            result = it
            return@collect
        }
        return result ?: throw NoSuchElementException("Flow is empty")
    }
}

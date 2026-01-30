package com.example.sonicflow.data.repository

import android.util.Log
import com.example.sonicflow.data.local.preferences.PreferencesManager
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlayerRepository
import com.example.sonicflow.service.AudioPlaybackService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : PlayerRepository {

    companion object {
        private const val TAG = "PlayerRepository"
    }

    private var audioService: AudioPlaybackService? = null

    // ✅ Exposer les flows du service (fallback sur états locaux si service pas connecté)
    private val _currentTrack = MutableStateFlow<Track?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _queue = MutableStateFlow<List<Track>>(emptyList())

    fun setAudioService(service: AudioPlaybackService) {
        audioService = service
        Log.d(TAG, "✅ AudioService connecté au PlayerRepository")
        observeServiceStates()
    }

    /**
     * ✅ Observer et synchroniser les états du service avec le repository
     */
    private fun observeServiceStates() {
        audioService?.let { service ->
            kotlinx.coroutines.GlobalScope.launch {
                service.currentTrack.collect { track ->
                    Log.d(TAG, "📡 Track reçue du service: ${track?.title}")
                    _currentTrack.value = track
                }
            }

            kotlinx.coroutines.GlobalScope.launch {
                service.isPlaying.collect { isPlaying ->
                    Log.d(TAG, "📡 isPlaying reçu du service: $isPlaying")
                    _isPlaying.value = isPlaying
                }
            }

            kotlinx.coroutines.GlobalScope.launch {
                service.currentPosition.collect { position ->
                    _currentPosition.value = position
                }
            }

            Log.d(TAG, "👀 Observation des états du service démarrée")
        }
    }

    override suspend fun playTrack(track: Track) {
        Log.d(TAG, "🎵 playTrack appelé: ${track.title}")

        if (audioService == null) {
            Log.e(TAG, "❌ AudioService est null!")
            throw IllegalStateException("AudioService not connected")
        }

        try {
            audioService?.playTrack(track)
            Log.d(TAG, "✅ AudioService.playTrack() appelé avec succès")

            // Mettre à jour immédiatement les états locaux
            _currentTrack.value = track
            _isPlaying.value = true
            _duration.value = track.duration
            _currentPosition.value = 0L

            preferencesManager.saveCurrentTrackId(track.id)
            preferencesManager.saveCurrentPosition(0L)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de playTrack: ${e.message}", e)
            throw e
        }
    }

    override suspend fun pauseTrack() {
        Log.d(TAG, "⏸️ pauseTrack appelé")
        audioService?.pause()
        _isPlaying.value = false
        preferencesManager.saveCurrentPosition(_currentPosition.value)
    }

    override suspend fun resumeTrack() {
        Log.d(TAG, "▶️ resumeTrack appelé")
        audioService?.play()
        _isPlaying.value = true
    }

    override suspend fun skipToNext() {
        Log.d(TAG, "⏭️ skipToNext appelé")

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
        Log.d(TAG, "⏮️ skipToPrevious appelé")

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
        Log.d(TAG, "⏩ seekTo: $position ms")
        _currentPosition.value = position
        preferencesManager.saveCurrentPosition(position)
    }

    // ✅ Exposer les flows synchronisés
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
        Log.d(TAG, "📋 setQueue: ${tracks.size} pistes")
        _queue.value = tracks
    }

    override suspend fun release() {
        Log.d(TAG, "🧹 release appelé")
        _currentTrack.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _queue.value = emptyList()
    }

    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        this.collect {
            result = it
            return@collect
        }
        return result ?: throw NoSuchElementException("Flow is empty")
    }
}
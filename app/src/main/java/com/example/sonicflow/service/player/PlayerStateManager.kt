package com.example.sonicflow.service.player

import com.example.sonicflow.domain.model.PlayerState
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère l'état global du player
 * Semaine 2, Jours 8-12
 */
@Singleton
class PlayerStateManager @Inject constructor() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun updateCurrentTrack(track: Track?) {
        _playerState.update { it.copy(currentTrack = track) }
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        _playerState.update { it.copy(isPlaying = isPlaying) }
    }

    fun updatePosition(position: Long) {
        _playerState.update {
            val duration = it.duration
            val progress = if (duration > 0) {
                (position.toFloat() / duration * 100).coerceIn(0f, 100f)
            } else {
                0f
            }

            it.copy(
                currentPosition = position,
                progress = progress
            )
        }
    }

    fun updateDuration(duration: Long) {
        _playerState.update { it.copy(duration = duration) }
    }

    fun updateQueue(queue: List<Track>, currentIndex: Int) {
        _playerState.update {
            it.copy(
                queue = queue,
                currentQueueIndex = currentIndex
            )
        }
    }

    fun updateRepeatMode(repeatMode: RepeatMode) {
        _playerState.update { it.copy(repeatMode = repeatMode) }
    }

    fun updateShuffleEnabled(enabled: Boolean) {
        _playerState.update { it.copy(shuffleEnabled = enabled) }
    }

    fun reset() {
        _playerState.value = PlayerState()
    }
}
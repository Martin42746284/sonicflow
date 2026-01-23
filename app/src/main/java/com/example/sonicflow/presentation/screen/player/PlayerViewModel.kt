package com.example.sonicflow.presentation.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.usecase.player.*
import com.example.sonicflow.domain.usecase.waveform.GenerateWaveformUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playTrackUseCase: PlayTrackUseCase,
    private val pauseTrackUseCase: PauseTrackUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val toggleRepeatModeUseCase: ToggleRepeatModeUseCase,
    private val toggleShuffleModeUseCase: ToggleShuffleModeUseCase,
    private val generateWaveformUseCase: GenerateWaveformUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerScreenState())
    val state: StateFlow<PlayerScreenState> = _state.asStateFlow()

    fun playTrack(
        track: Track,
        queue: List<Track> = listOf(track)
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val trackIndex = queue.indexOf(track).coerceAtLeast(0)

                playTrackUseCase(track)

                _state.update { currentState ->
                    currentState.copy(
                        currentTrack = track,
                        isPlaying = true,
                        queue = queue,
                        currentQueueIndex = trackIndex,
                        currentPosition = 0L,
                        duration = track.duration,
                        isLoading = false,
                        hasNext = hasNext(trackIndex, queue, currentState.repeatMode),
                        hasPrevious = hasPrevious(trackIndex, currentState.repeatMode)
                    )
                }

                loadWaveform(track)
            } catch (exception: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to play track"
                )}
            }
        }
    }

    fun togglePlayPause() {
        val currentState = _state.value

        viewModelScope.launch {
            try {
                if (currentState.isPlaying) {
                    pauseTrackUseCase()
                    _state.update { it.copy(isPlaying = false) }
                } else {
                    currentState.currentTrack?.let { track ->
                        playTrackUseCase(track)
                        _state.update { it.copy(isPlaying = true) }
                    }
                }
            } catch (exception: Exception) {
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle playback"
                )}
            }
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            val currentState = _state.value
            val nextIndex = currentState.currentQueueIndex + 1

            if (nextIndex < currentState.queue.size) {
                val nextTrack = currentState.queue[nextIndex]
                playTrack(track = nextTrack, queue = currentState.queue)
            } else if (currentState.repeatMode == RepeatMode.ALL) {
                val firstTrack = currentState.queue.firstOrNull()
                firstTrack?.let { playTrack(track = it, queue = currentState.queue) }
            } else {
                _state.update { it.copy(error = "No next track available") }
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.currentPosition > 3000L) {
                seekTo(0L)
            } else {
                val previousIndex = currentState.currentQueueIndex - 1

                if (previousIndex >= 0) {
                    val previousTrack = currentState.queue[previousIndex]
                    playTrack(track = previousTrack, queue = currentState.queue)
                } else if (currentState.repeatMode == RepeatMode.ALL) {
                    val lastTrack = currentState.queue.lastOrNull()
                    lastTrack?.let { playTrack(track = it, queue = currentState.queue) }
                }
            }
        }
    }

    fun seekTo(position: Long) {
        val currentState = _state.value
        val validPosition = position.coerceIn(0L, currentState.duration)

        viewModelScope.launch {
            try {
                seekToPositionUseCase(validPosition)  // ✅ Un seul paramètre
                _state.update { it.copy(currentPosition = validPosition) }
            } catch (exception: Exception) {
                _state.update { it.copy(
                    error = exception.message ?: "Failed to seek"
                )}
            }
        }
    }

    fun seekToPercentage(percentage: Float) {
        val currentState = _state.value
        val position = (currentState.duration * percentage.coerceIn(0f, 1f)).toLong()
        seekTo(position)
    }

    fun toggleRepeatMode() {
        viewModelScope.launch {
            val currentMode = _state.value.repeatMode
            val newMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }

            try {
                toggleRepeatModeUseCase()

                val currentState = _state.value
                _state.update { it.copy(
                    repeatMode = newMode,
                    hasNext = hasNext(currentState.currentQueueIndex, currentState.queue, newMode),
                    hasPrevious = hasPrevious(currentState.currentQueueIndex, newMode)
                )}
            } catch (exception: Exception) {
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle repeat mode"
                )}
            }
        }
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            val currentState = _state.value
            val newShuffleState = !currentState.shuffleEnabled

            try {
                val newQueue = if (newShuffleState) {
                    val currentTrack = currentState.currentTrack
                    val remainingTracks = currentState.queue.toMutableList()

                    if (currentTrack != null) {
                        remainingTracks.remove(currentTrack)
                        remainingTracks.shuffle()
                        listOf(currentTrack) + remainingTracks
                    } else {
                        remainingTracks.shuffled()
                    }
                } else {
                    currentState.queue
                }

                toggleShuffleModeUseCase()

                _state.update { it.copy(
                    shuffleEnabled = newShuffleState,
                    queue = newQueue,
                    currentQueueIndex = 0,
                    hasNext = hasNext(0, newQueue, currentState.repeatMode),
                    hasPrevious = hasPrevious(0, currentState.repeatMode)
                )}
            } catch (exception: Exception) {
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle shuffle"
                )}
            }
        }
    }

    fun playTrackAtIndex(index: Int) {
        val currentState = _state.value
        if (index in currentState.queue.indices) {
            playTrack(
                track = currentState.queue[index],
                queue = currentState.queue
            )
        }
    }

    private fun loadWaveform(track: Track) {
        viewModelScope.launch {
            try {
                if (!track.waveformData.isNullOrBlank()) {
                    val amplitudes = parseWaveformData(track.waveformData)
                    _state.update { it.copy(waveformData = amplitudes) }
                } else {
                    // Générer la waveform
                    val waveformJson = generateWaveformUseCase(
                        audioPath = track.path,
                        samplesCount = 100
                    )

                    val amplitudes = parseWaveformData(waveformJson)
                    _state.update { it.copy(waveformData = amplitudes) }
                }
            } catch (exception: Exception) {
                val defaultWaveform = generateDefaultWaveform(samplesCount = 100)
                _state.update { it.copy(waveformData = defaultWaveform) }
            }
        }
    }

    private fun parseWaveformData(waveformJson: String): List<Float> {
        return try {
            waveformJson
                .removeSurrounding("[", "]")
                .split(",")
                .mapNotNull { it.trim().toFloatOrNull() }
        } catch (e: Exception) {
            generateDefaultWaveform(samplesCount = 100)
        }
    }

    private fun generateDefaultWaveform(samplesCount: Int): List<Float> {
        return List(samplesCount) { (0.1f + Math.random().toFloat() * 0.9f) }
    }

    private fun hasNext(
        currentIndex: Int,
        queue: List<Track>,
        repeatMode: RepeatMode
    ): Boolean {
        return when (repeatMode) {
            RepeatMode.OFF -> currentIndex < queue.lastIndex
            RepeatMode.ONE, RepeatMode.ALL -> true
        }
    }

    private fun hasPrevious(
        currentIndex: Int,
        repeatMode: RepeatMode
    ): Boolean {
        return when (repeatMode) {
            RepeatMode.OFF -> currentIndex > 0
            RepeatMode.ONE, RepeatMode.ALL -> true
        }
    }

    fun updatePosition(position: Long) {
        val currentState = _state.value
        val progress = if (currentState.duration > 0) {
            (position.toFloat() / currentState.duration) * 100f
        } else {
            0f
        }

        _state.update { it.copy(
            currentPosition = position,
            progress = progress
        )}
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class PlayerScreenState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val waveformData: List<Float> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
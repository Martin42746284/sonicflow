package com.example.sonicflow.presentation.screen.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlayerRepository
import com.example.sonicflow.domain.usecase.player.*
import com.example.sonicflow.domain.usecase.waveform.GenerateWaveformUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val playTrackUseCase: PlayTrackUseCase,
    private val pauseTrackUseCase: PauseTrackUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val toggleRepeatModeUseCase: ToggleRepeatModeUseCase,
    private val toggleShuffleModeUseCase: ToggleShuffleModeUseCase,
    private val generateWaveformUseCase: GenerateWaveformUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _state = MutableStateFlow(PlayerScreenState())
    val state: StateFlow<PlayerScreenState> = _state.asStateFlow()

    init {
        Log.d(TAG, "🎬 PlayerViewModel créé")
        observeRepositoryState()
    }

    /**
     * ✅ Observer l'état du repository (qui est synchronisé avec le service)
     */
    private fun observeRepositoryState() {
        Log.d(TAG, "👀 Début de l'observation du repository")

        // Observer la piste courante
        viewModelScope.launch {
            playerRepository.getCurrentTrack().collect { track ->
                Log.d(TAG, "📡 Track mise à jour depuis repository: ${track?.title}")
                if (track != null) {
                    _state.update { it.copy(
                        currentTrack = track,
                        duration = track.duration
                    )}
                }
            }
        }

        // Observer l'état de lecture
        viewModelScope.launch {
            playerRepository.isPlaying().collect { isPlaying ->
                Log.d(TAG, "📡 isPlaying mise à jour depuis repository: $isPlaying")
                _state.update { it.copy(isPlaying = isPlaying) }
            }
        }

        // Observer la position
        viewModelScope.launch {
            playerRepository.getCurrentPosition().collect { position ->
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
        }

        // Observer le mode repeat
        viewModelScope.launch {
            playerRepository.getRepeatMode().collect { mode ->
                Log.d(TAG, "📡 RepeatMode mise à jour: $mode")
                _state.update { it.copy(repeatMode = mode) }
            }
        }

        // Observer le shuffle
        viewModelScope.launch {
            playerRepository.getShuffleEnabled().collect { enabled ->
                Log.d(TAG, "📡 Shuffle mise à jour: $enabled")
                _state.update { it.copy(shuffleEnabled = enabled) }
            }
        }

        // Observer la queue
        viewModelScope.launch {
            playerRepository.getQueue().collect { queue ->
                Log.d(TAG, "📡 Queue mise à jour: ${queue.size} pistes")
                _state.update { it.copy(queue = queue) }
            }
        }
    }

    fun playTrack(
        track: Track,
        queue: List<Track> = listOf(track)
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🎵 playTrack appelé: ${track.title}")
            _state.update { it.copy(isLoading = true) }

            try {
                val trackIndex = queue.indexOf(track).coerceAtLeast(0)

                // Mettre à jour la queue dans le repository
                playerRepository.setQueue(queue)

                // Lancer la lecture
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
                Log.d(TAG, "✅ playTrack terminé")
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur playTrack: ${exception.message}", exception)
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
                    Log.d(TAG, "⏸️ Pause")
                    pauseTrackUseCase()
                } else {
                    Log.d(TAG, "▶️ Play")
                    currentState.currentTrack?.let { track ->
                        playTrackUseCase(track)
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur togglePlayPause: ${exception.message}")
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
                Log.d(TAG, "⚠️ Pas de piste suivante")
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
                seekToPositionUseCase(validPosition)
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur seek: ${exception.message}")
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
            try {
                toggleRepeatModeUseCase()

                val currentState = _state.value
                _state.update { it.copy(
                    hasNext = hasNext(currentState.currentQueueIndex, currentState.queue, currentState.repeatMode),
                    hasPrevious = hasPrevious(currentState.currentQueueIndex, currentState.repeatMode)
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
                playerRepository.setQueue(newQueue)

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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 PlayerViewModel cleared")
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
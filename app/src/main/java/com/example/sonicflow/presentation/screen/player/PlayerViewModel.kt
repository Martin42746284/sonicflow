package com.example.sonicflow.presentation.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.PlayerState
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.usecase.player.*
import com.example.sonicflow.domain.usecase.waveform.GenerateWaveformUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran Player
 * Semaine 2, Jours 13-14 : Logique du lecteur
 * Semaine 4, Jours 22-26 : Waveform
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playTrackUseCase: PlayTrackUseCase,
    private val pauseTrackUseCase: PauseTrackUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val toggleRepeatModeUseCase: ToggleRepeatModeUseCase,
    private val toggleShuffleModeUseCase: ToggleShuffleModeUseCase,
    private val updateQueueUseCase: UpdateQueueUseCase,
    private val generateWaveformUseCase: GenerateWaveformUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerScreenState())
    val state: StateFlow<PlayerScreenState> = _state.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            _playerState.collect { playerState ->
                _state.update { it.copy(
                    currentTrack = playerState.currentTrack,
                    isPlaying = playerState.isPlaying,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    progress = playerState.progress,
                    repeatMode = playerState.repeatMode,
                    shuffleEnabled = playerState.shuffleEnabled,
                    queue = playerState.queue,
                    currentQueueIndex = playerState.currentQueueIndex,
                    hasNext = playerState.hasNext(),
                    hasPrevious = playerState.hasPrevious()
                )}
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Mettre à jour la queue
            val (newQueue, index) = updateQueueUseCase(queue, queue.indexOf(track))

            // Jouer la piste
            playTrackUseCase(track).fold(
                onSuccess = {
                    _playerState.update { state ->
                        state.copy(
                            currentTrack = track,
                            isPlaying = true,
                            queue = newQueue,
                            currentQueueIndex = index,
                            currentPosition = 0L,
                            duration = track.duration
                        )
                    }

                    // Générer la waveform
                    loadWaveform(track)

                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to play track"
                    )}
                }
            )
        }
    }

    fun togglePlayPause() {
        val currentState = _playerState.value

        if (currentState.isPlaying) {
            // Pause
            val position = pauseTrackUseCase(currentState.currentPosition)
            _playerState.update { it.copy(
                isPlaying = false,
                currentPosition = position
            )}
        } else {
            // Play
            _playerState.update { it.copy(isPlaying = true) }
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            val currentState = _playerState.value
            val nextTrack = skipToNextUseCase(currentState)

            nextTrack?.let { track ->
                playTrack(track, currentState.queue)
            } ?: run {
                _state.update { it.copy(error = "No next track available") }
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            val currentState = _playerState.value
            val previousTrack = skipToPreviousUseCase(
                currentState,
                currentState.currentPosition
            )

            previousTrack?.let { track ->
                if (track.id == currentState.currentTrack?.id && currentState.currentPosition > 3000) {
                    // Recommencer la piste actuelle
                    seekTo(0L)
                } else {
                    playTrack(track, currentState.queue)
                }
            }
        }
    }

    fun seekTo(position: Long) {
        val currentState = _playerState.value
        val validPosition = seekToPositionUseCase(position, currentState.duration)

        _playerState.update { it.copy(currentPosition = validPosition) }
    }

    fun seekToPercentage(percentage: Float) {
        val currentState = _playerState.value
        val position = seekToPositionUseCase.seekByPercentage(percentage, currentState.duration)
        seekTo(position)
    }

    fun toggleRepeatMode() {
        val currentMode = _playerState.value.repeatMode
        val newMode = toggleRepeatModeUseCase(currentMode)

        _playerState.update { it.copy(repeatMode = newMode) }
    }

    fun toggleShuffle() {
        val currentState = _playerState.value
        val (newShuffleState, newQueue) = toggleShuffleModeUseCase(
            currentState.shuffleEnabled,
            currentState.queue,
            currentState.currentTrack
        )

        _playerState.update { it.copy(
            shuffleEnabled = newShuffleState,
            queue = newQueue,
            currentQueueIndex = 0
        )}
    }

    fun playTrackAtIndex(index: Int) {
        val queue = _playerState.value.queue
        if (index in queue.indices) {
            playTrack(queue[index], queue)
        }
    }

    private fun loadWaveform(track: Track) {
        viewModelScope.launch {
            // Vérifier si la waveform existe déjà
            if (track.hasWaveform()) {
                val amplitudes = generateWaveformUseCase.parseWaveformData(track.waveformData!!)
                _state.update { it.copy(waveformData = amplitudes) }
            } else {
                // Générer la waveform
                generateWaveformUseCase(
                    trackId = track.id,
                    audioPath = track.path,
                    samplesCount = 100
                ).fold(
                    onSuccess = { waveformJson ->
                        val amplitudes = generateWaveformUseCase.parseWaveformData(waveformJson)
                        _state.update { it.copy(waveformData = amplitudes) }
                    },
                    onFailure = {
                        // Utiliser une waveform par défaut
                        val defaultWaveform = generateWaveformUseCase.generateDefaultWaveform(100)
                        _state.update { it.copy(waveformData = defaultWaveform) }
                    }
                )
            }
        }
    }

    fun updatePosition(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
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

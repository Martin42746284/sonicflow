package com.example.sonicflow.presentation.screen.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.usecase.player.*
import com.example.sonicflow.domain.usecase.waveform.GenerateWaveformUseCase
import com.example.sonicflow.service.AudioPlaybackService
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

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _state = MutableStateFlow(PlayerScreenState())
    val state: StateFlow<PlayerScreenState> = _state.asStateFlow()

    // ✅ Référence au service audio (sera injectée depuis l'UI)
    private var audioService: AudioPlaybackService? = null

    /**
     * ✅ Connecter le service audio au ViewModel
     * Appelée depuis MainActivity ou PlayerScreen
     */
    fun setAudioService(service: AudioPlaybackService) {
        audioService = service
        Log.d(TAG, "✅ AudioService connecté au PlayerViewModel")
    }

    /**
     * ✅ Jouer une piste avec queue
     */
    fun playTrack(
        track: Track,
        queue: List<Track> = listOf(track)
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🎵 Tentative de lecture: ${track.title}")
            Log.d(TAG, "📂 Chemin: ${track.path}")

            _state.update { it.copy(isLoading = true) }

            try {
                val trackIndex = queue.indexOf(track).coerceAtLeast(0)

                // ✅ Appeler le use case
                playTrackUseCase(track)
                Log.d(TAG, "✅ PlayTrackUseCase appelé avec succès")

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
                        hasPrevious = hasPrevious(trackIndex, currentState.repeatMode),
                        error = null
                    )
                }

                // Charger la waveform
                loadWaveform(track)

                Log.d(TAG, "✅ État mis à jour, lecture en cours")
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur lors de la lecture: ${exception.message}", exception)
                _state.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to play track"
                )}
            }
        }
    }

    /**
     * ✅ Toggle Play/Pause
     */
    fun togglePlayPause() {
        val currentState = _state.value
        Log.d(TAG, "🔄 Toggle Play/Pause - État actuel: ${currentState.isPlaying}")

        viewModelScope.launch {
            try {
                if (currentState.isPlaying) {
                    pauseTrackUseCase()
                    _state.update { it.copy(isPlaying = false) }
                    Log.d(TAG, "⏸️ Lecture mise en pause")
                } else {
                    currentState.currentTrack?.let { track ->
                        playTrackUseCase(track)
                        _state.update { it.copy(isPlaying = true) }
                        Log.d(TAG, "▶️ Lecture reprise")
                    } ?: run {
                        Log.w(TAG, "⚠️ Aucune piste à jouer")
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur toggle play/pause: ${exception.message}", exception)
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle playback"
                )}
            }
        }
    }

    /**
     * ✅ Passer à la piste suivante
     */
    fun skipToNext() {
        viewModelScope.launch {
            Log.d(TAG, "⏭️ Skip to next")

            val currentState = _state.value
            val nextIndex = currentState.currentQueueIndex + 1

            if (nextIndex < currentState.queue.size) {
                val nextTrack = currentState.queue[nextIndex]
                Log.d(TAG, "🎵 Piste suivante: ${nextTrack.title}")
                playTrack(track = nextTrack, queue = currentState.queue)
            } else if (currentState.repeatMode == RepeatMode.ALL) {
                val firstTrack = currentState.queue.firstOrNull()
                firstTrack?.let {
                    Log.d(TAG, "🔁 Retour au début de la queue")
                    playTrack(track = it, queue = currentState.queue)
                }
            } else {
                Log.w(TAG, "⚠️ Aucune piste suivante disponible")
                _state.update { it.copy(error = "No next track available") }
            }
        }
    }

    /**
     * ✅ Revenir à la piste précédente
     */
    fun skipToPrevious() {
        viewModelScope.launch {
            Log.d(TAG, "⏮️ Skip to previous")

            val currentState = _state.value

            // Si on est à plus de 3 secondes, revenir au début
            if (currentState.currentPosition > 3000L) {
                Log.d(TAG, "🔄 Retour au début de la piste")
                seekTo(0L)
            } else {
                val previousIndex = currentState.currentQueueIndex - 1

                if (previousIndex >= 0) {
                    val previousTrack = currentState.queue[previousIndex]
                    Log.d(TAG, "🎵 Piste précédente: ${previousTrack.title}")
                    playTrack(track = previousTrack, queue = currentState.queue)
                } else if (currentState.repeatMode == RepeatMode.ALL) {
                    val lastTrack = currentState.queue.lastOrNull()
                    lastTrack?.let {
                        Log.d(TAG, "🔁 Retour à la fin de la queue")
                        playTrack(track = it, queue = currentState.queue)
                    }
                } else {
                    Log.w(TAG, "⚠️ Aucune piste précédente disponible")
                }
            }
        }
    }

    /**
     * ✅ Seek à une position spécifique
     */
    fun seekTo(position: Long) {
        val currentState = _state.value
        val validPosition = position.coerceIn(0L, currentState.duration)

        Log.d(TAG, "⏩ Seek to: $validPosition ms")

        viewModelScope.launch {
            try {
                seekToPositionUseCase(validPosition)
                _state.update { it.copy(currentPosition = validPosition) }
                Log.d(TAG, "✅ Seek effectué")
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur seek: ${exception.message}", exception)
                _state.update { it.copy(
                    error = exception.message ?: "Failed to seek"
                )}
            }
        }
    }

    /**
     * ✅ Seek par pourcentage (0.0 à 1.0)
     */
    fun seekToPercentage(percentage: Float) {
        val currentState = _state.value
        val position = (currentState.duration * percentage.coerceIn(0f, 1f)).toLong()
        Log.d(TAG, "⏩ Seek to percentage: ${percentage * 100}% = $position ms")
        seekTo(position)
    }

    /**
     * ✅ Toggle repeat mode (OFF → ALL → ONE → OFF)
     */
    fun toggleRepeatMode() {
        viewModelScope.launch {
            val currentMode = _state.value.repeatMode
            val newMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }

            Log.d(TAG, "🔁 Repeat mode: $currentMode → $newMode")

            try {
                toggleRepeatModeUseCase()

                val currentState = _state.value
                _state.update { it.copy(
                    repeatMode = newMode,
                    hasNext = hasNext(currentState.currentQueueIndex, currentState.queue, newMode),
                    hasPrevious = hasPrevious(currentState.currentQueueIndex, newMode)
                )}
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur toggle repeat: ${exception.message}", exception)
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle repeat mode"
                )}
            }
        }
    }

    /**
     * ✅ Toggle shuffle
     */
    fun toggleShuffle() {
        viewModelScope.launch {
            val currentState = _state.value
            val newShuffleState = !currentState.shuffleEnabled

            Log.d(TAG, "🔀 Shuffle: ${currentState.shuffleEnabled} → $newShuffleState")

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

                Log.d(TAG, "✅ Queue mélangée: ${newQueue.size} pistes")
            } catch (exception: Exception) {
                Log.e(TAG, "❌ Erreur toggle shuffle: ${exception.message}", exception)
                _state.update { it.copy(
                    error = exception.message ?: "Failed to toggle shuffle"
                )}
            }
        }
    }

    /**
     * ✅ Jouer une piste à un index spécifique dans la queue
     */
    fun playTrackAtIndex(index: Int) {
        val currentState = _state.value
        if (index in currentState.queue.indices) {
            val track = currentState.queue[index]
            Log.d(TAG, "🎵 Lecture à l'index $index: ${track.title}")
            playTrack(
                track = track,
                queue = currentState.queue
            )
        } else {
            Log.w(TAG, "⚠️ Index invalide: $index (queue size: ${currentState.queue.size})")
        }
    }

    /**
     * ✅ Charger la waveform
     */
    private fun loadWaveform(track: Track) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📊 Chargement waveform pour: ${track.title}")

                if (!track.waveformData.isNullOrBlank()) {
                    val amplitudes = parseWaveformData(track.waveformData)
                    _state.update { it.copy(waveformData = amplitudes) }
                    Log.d(TAG, "✅ Waveform chargée depuis DB: ${amplitudes.size} samples")
                } else {
                    // Générer la waveform
                    val waveformJson = generateWaveformUseCase(
                        audioPath = track.path,
                        samplesCount = 100
                    )

                    val amplitudes = parseWaveformData(waveformJson)
                    _state.update { it.copy(waveformData = amplitudes) }
                    Log.d(TAG, "✅ Waveform générée: ${amplitudes.size} samples")
                }
            } catch (exception: Exception) {
                Log.w(TAG, "⚠️ Impossible de charger la waveform, utilisation par défaut: ${exception.message}")
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
            Log.e(TAG, "❌ Erreur parsing waveform: ${e.message}")
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

    /**
     * ✅ Mettre à jour la position courante (appelée périodiquement depuis le service)
     */
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

    /**
     * ✅ Effacer l'erreur
     */
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
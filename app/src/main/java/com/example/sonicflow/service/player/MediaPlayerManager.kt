package com.example.sonicflow.service.player

import android.content.Context
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager pour gérer la logique de lecture audio avec ExoPlayer
 * Semaine 2, Jours 8-10 : Intégration ExoPlayer
 */
@Singleton
class MediaPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val playerStateManager: PlayerStateManager
) {

    private var currentQueue: List<Track> = emptyList()
    private var originalQueue: List<Track> = emptyList()
    private var currentTrackIndex: Int = -1

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var repeatMode = RepeatMode.OFF
    private var shuffleEnabled = false

    init {
        setupPlayerListeners()
    }

    /**
     * Configure les listeners ExoPlayer
     */
    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                        playerStateManager.updateDuration(exoPlayer.duration)
                    }
                    Player.STATE_ENDED -> {
                        handleTrackEnded()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                playerStateManager.updateIsPlaying(isPlaying)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updateCurrentPosition()
            }
        })
    }

    /**
     * Joue une piste avec une queue optionnelle
     */
    fun playTrack(track: Track, queue: List<Track> = listOf(track), startIndex: Int = 0) {
        currentQueue = queue
        originalQueue = queue.toList()
        currentTrackIndex = startIndex

        _currentTrack.value = track
        playerStateManager.updateCurrentTrack(track)
        playerStateManager.updateQueue(currentQueue, currentTrackIndex)

        // Créer MediaItem
        val mediaItem = createMediaItem(track)

        // Préparer et jouer
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    /**
     * Joue une queue de pistes
     */
    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return

        currentQueue = tracks
        originalQueue = tracks.toList()
        currentTrackIndex = startIndex.coerceIn(0, tracks.lastIndex)

        val track = tracks[currentTrackIndex]
        _currentTrack.value = track
        playerStateManager.updateCurrentTrack(track)
        playerStateManager.updateQueue(currentQueue, currentTrackIndex)

        // Créer tous les MediaItems
        val mediaItems = tracks.map { createMediaItem(it) }

        // Configurer la queue
        exoPlayer.setMediaItems(mediaItems, currentTrackIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    /**
     * Met la lecture en pause
     */
    fun pause() {
        exoPlayer.playWhenReady = false
    }

    /**
     * Reprend la lecture
     */
    fun play() {
        exoPlayer.playWhenReady = true
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Passe à la piste suivante
     */
    fun skipToNext(): Track? {
        return when (repeatMode) {
            RepeatMode.ONE -> {
                // Recommencer la piste actuelle
                seekTo(0L)
                _currentTrack.value
            }
            RepeatMode.ALL -> {
                // Passer à la suivante, ou revenir au début
                val nextIndex = (currentTrackIndex + 1) % currentQueue.size
                playTrackAtIndex(nextIndex)
            }
            RepeatMode.OFF -> {
                // Passer à la suivante si elle existe
                if (currentTrackIndex < currentQueue.lastIndex) {
                    playTrackAtIndex(currentTrackIndex + 1)
                } else {
                    null
                }
            }
        }
    }

    /**
     * Revient à la piste précédente
     */
    fun skipToPrevious(): Track? {
        // Si on est à plus de 3 secondes, recommencer la piste actuelle
        if (_currentPosition.value > 3000L) {
            seekTo(0L)
            return _currentTrack.value
        }

        // Sinon, aller à la piste précédente
        return if (currentTrackIndex > 0) {
            playTrackAtIndex(currentTrackIndex - 1)
        } else if (repeatMode == RepeatMode.ALL) {
            // Si repeat all, aller à la dernière piste
            playTrackAtIndex(currentQueue.lastIndex)
        } else {
            null
        }
    }

    /**
     * Joue une piste à un index spécifique
     */
    fun playTrackAtIndex(index: Int): Track? {
        if (index !in currentQueue.indices) return null

        currentTrackIndex = index
        val track = currentQueue[index]

        _currentTrack.value = track
        playerStateManager.updateCurrentTrack(track)
        playerStateManager.updateQueue(currentQueue, currentTrackIndex)

        exoPlayer.seekToDefaultPosition(index)
        exoPlayer.playWhenReady = true

        return track
    }

    /**
     * Seek à une position spécifique
     */
    fun seekTo(position: Long) {
        val validPosition = position.coerceIn(0L, _duration.value)
        exoPlayer.seekTo(validPosition)
        _currentPosition.value = validPosition
        playerStateManager.updatePosition(validPosition)
    }

    /**
     * Seek par pourcentage (0.0 - 1.0)
     */
    fun seekToPercentage(percentage: Float) {
        val position = (_duration.value * percentage.coerceIn(0f, 1f)).toLong()
        seekTo(position)
    }

    /**
     * Change le mode repeat
     */
    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
        playerStateManager.updateRepeatMode(mode)

        // Configurer ExoPlayer repeat mode
        exoPlayer.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeatMode(): RepeatMode {
        val newMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(newMode)
        return newMode
    }

    /**
     * Active/désactive le shuffle
     */
    fun setShuffleEnabled(enabled: Boolean) {
        shuffleEnabled = enabled
        playerStateManager.updateShuffleEnabled(enabled)

        if (enabled) {
            // Mélanger la queue en gardant la piste actuelle en première position
            val currentTrack = _currentTrack.value
            val remainingTracks = currentQueue.toMutableList()

            if (currentTrack != null) {
                remainingTracks.remove(currentTrack)
                remainingTracks.shuffle()
                currentQueue = listOf(currentTrack) + remainingTracks
                currentTrackIndex = 0
            } else {
                currentQueue = remainingTracks.shuffled()
            }

            exoPlayer.shuffleModeEnabled = true
        } else {
            // Restaurer l'ordre original
            val currentTrack = _currentTrack.value
            currentQueue = originalQueue.toList()
            currentTrackIndex = currentQueue.indexOf(currentTrack).coerceAtLeast(0)

            exoPlayer.shuffleModeEnabled = false
        }

        playerStateManager.updateQueue(currentQueue, currentTrackIndex)
    }

    /**
     * Toggle shuffle
     */
    fun toggleShuffle(): Boolean {
        setShuffleEnabled(!shuffleEnabled)
        return shuffleEnabled
    }

    /**
     * Met à jour la position actuelle
     */
    fun updateCurrentPosition() {
        if (_isPlaying.value) {
            _currentPosition.value = exoPlayer.currentPosition
            playerStateManager.updatePosition(exoPlayer.currentPosition)
        }
    }

    /**
     * Gère la fin d'une piste
     */
    private fun handleTrackEnded() {
        when (repeatMode) {
            RepeatMode.ONE -> {
                // Recommencer la même piste
                seekTo(0L)
                play()
            }
            RepeatMode.ALL -> {
                // Passer à la suivante
                skipToNext()
            }
            RepeatMode.OFF -> {
                // Passer à la suivante si elle existe, sinon arrêter
                if (currentTrackIndex < currentQueue.lastIndex) {
                    skipToNext()
                } else {
                    pause()
                }
            }
        }
    }

    /**
     * Crée un MediaItem depuis une Track
     */
    private fun createMediaItem(track: Track): MediaItem {
        return MediaItem.Builder()
            .setUri(track.path)
            .setMediaId(track.id.toString())
            .setMediaMetadata(
                com.google.android.exoplayer2.MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(android.net.Uri.parse(track.albumArtUri ?: ""))
                    .build()
            )
            .build()
    }

    /**
     * Récupère la queue actuelle
     */
    fun getCurrentQueue(): List<Track> = currentQueue

    /**
     * Récupère l'index actuel
     */
    fun getCurrentIndex(): Int = currentTrackIndex

    /**
     * Vérifie s'il y a une piste suivante
     */
    fun hasNext(): Boolean {
        return when (repeatMode) {
            RepeatMode.OFF -> currentTrackIndex < currentQueue.lastIndex
            RepeatMode.ONE, RepeatMode.ALL -> true
        }
    }

    /**
     * Vérifie s'il y a une piste précédente
     */
    fun hasPrevious(): Boolean {
        return when (repeatMode) {
            RepeatMode.OFF -> currentTrackIndex > 0
            RepeatMode.ONE, RepeatMode.ALL -> true
        }
    }

    /**
     * Arrête complètement la lecture
     */
    fun stop() {
        exoPlayer.stop()
        _isPlaying.value = false
        _currentPosition.value = 0L
    }

    /**
     * Libère les ressources
     */
    fun release() {
        exoPlayer.release()
        playerStateManager.reset()
    }

    /**
     * Récupère le repeat mode actuel
     */
    fun getRepeatMode(): RepeatMode = repeatMode

    /**
     * Vérifie si le shuffle est activé
     */
    fun isShuffleEnabled(): Boolean = shuffleEnabled
}

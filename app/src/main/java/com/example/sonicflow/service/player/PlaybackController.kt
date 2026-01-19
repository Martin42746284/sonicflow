package com.example.sonicflow.service.player

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.sonicflow.service.AudioPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contrôleur pour interagir avec AudioPlaybackService
 * Interface entre l'UI et le service
 * Semaine 2, Jours 8-12
 */
@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null

    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState: StateFlow<PlaybackStateCompat?> = _playbackState.asStateFlow()

    private val _currentMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentMetadata: StateFlow<MediaMetadataCompat?> = _currentMetadata.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser?.sessionToken?.let { token ->
                mediaController = MediaControllerCompat(context, token).apply {
                    registerCallback(controllerCallback)
                }
                _isConnected.value = true

                // Récupérer l'état initial
                _playbackState.value = mediaController?.playbackState
                _currentMetadata.value = mediaController?.metadata
            }
        }

        override fun onConnectionSuspended() {
            _isConnected.value = false
        }

        override fun onConnectionFailed() {
            _isConnected.value = false
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentMetadata.value = metadata
        }
    }

    /**
     * Connecte au service
     */
    fun connect() {
        if (mediaBrowser == null) {
            mediaBrowser = MediaBrowserCompat(
                context,
                ComponentName(context, AudioPlaybackService::class.java),
                connectionCallback,
                null
            ).apply {
                connect()
            }
        }
    }

    /**
     * Déconnecte du service
     */
    fun disconnect() {
        mediaController?.unregisterCallback(controllerCallback)
        mediaBrowser?.disconnect()
        mediaBrowser = null
        mediaController = null
        _isConnected.value = false
    }

    /**
     * Joue ou met en pause
     */
    fun playPause() {
        val controller = mediaController ?: return
        val state = controller.playbackState

        if (state?.state == PlaybackStateCompat.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    /**
     * Joue une piste
     */
    fun play() {
        mediaController?.transportControls?.play()
    }

    /**
     * Met en pause
     */
    fun pause() {
        mediaController?.transportControls?.pause()
    }

    /**
     * Passe à la piste suivante
     */
    fun skipToNext() {
        mediaController?.transportControls?.skipToNext()
    }

    /**
     * Revient à la piste précédente
     */
    fun skipToPrevious() {
        mediaController?.transportControls?.skipToPrevious()
    }

    /**
     * Seek à une position
     */
    fun seekTo(position: Long) {
        mediaController?.transportControls?.seekTo(position)
    }

    /**
     * Arrête la lecture
     */
    fun stop() {
        mediaController?.transportControls?.stop()
    }

    /**
     * Vérifie si le player est en train de jouer
     */
    fun isPlaying(): Boolean {
        return playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING
    }

    /**
     * Récupère la position actuelle
     */
    fun getCurrentPosition(): Long {
        return playbackState.value?.position ?: 0L
    }

    /**
     * Récupère la durée totale
     */
    fun getDuration(): Long {
        return currentMetadata.value?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0L
    }
}
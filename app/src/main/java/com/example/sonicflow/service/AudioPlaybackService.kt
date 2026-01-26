package com.example.sonicflow.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.util.Constants
import com.example.sonicflow.service.notification.MediaNotificationManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Service de lecture audio en arrière-plan
 */
@AndroidEntryPoint
class AudioPlaybackService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationManager: MediaNotificationManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private var isForegroundService = false

    companion object {
        private const val MEDIA_ROOT_ID = "root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    }

    // LocalBinder pour la connexion depuis MainActivity
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == "android.media.browse.MediaBrowserService") {
            super.onBind(intent)
        } else {
            binder
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Créer la MediaSession
        val sessionActivityPendingIntent = packageManager
            ?.getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        mediaSession = MediaSessionCompat(this, "AudioPlaybackService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        // Connecter ExoPlayer à MediaSession
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                    return getMediaDescription(_currentTrack.value)
                }
            })
        }

        // Créer le notification manager
        notificationManager = MediaNotificationManager(this, mediaSession)

        // Listener pour les changements d'état du player
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (exoPlayer.playWhenReady) {
                            startForegroundService()
                        }
                    }
                    Player.STATE_ENDED -> {
                        // Piste terminée, passer à la suivante
                        skipToNext()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                updateNotification()

                if (isPlaying) {
                    startPositionUpdate()
                } else {
                    stopPositionUpdate()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // Gérer les erreurs de lecture
                stopForegroundService()
            }
        })

        // Callback pour les actions de la MediaSession
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                exoPlayer.playWhenReady = true
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }

            override fun onPause() {
                exoPlayer.playWhenReady = false
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            override fun onStop() {
                stopSelf()
            }

            override fun onSkipToNext() {
                skipToNext()
            }

            override fun onSkipToPrevious() {
                skipToPrevious()
            }

            override fun onSeekTo(pos: Long) {
                exoPlayer.seekTo(pos)
                _currentPosition.value = pos
            }
        })
    }

    /**
     * Joue une piste
     */
    fun playTrack(track: Track) {
        serviceScope.launch {
            _currentTrack.value = track

            // Créer MediaItem depuis la piste
            val mediaItem = MediaItem.Builder()
                .setUri(track.path)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(Uri.parse(track.albumArtUri))
                        .build()
                )
                .build()

            // Préparer et jouer
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            updateMediaSessionMetadata(track)
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    /**
     * Met à jour la queue de lecture
     */
    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.path)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
    }

    /**
     * Passe à la piste suivante
     */
    private fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
    }

    /**
     * Revient à la piste précédente
     */
    private fun skipToPrevious() {
        if (exoPlayer.currentPosition > Constants.Player.SEEK_BACK_THRESHOLD_MS) {
            // Si on est à plus de 3s, recommencer la piste actuelle
            exoPlayer.seekTo(0)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    /**
     * Met à jour les métadonnées de la MediaSession
     */
    private fun updateMediaSessionMetadata(track: Track) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.albumArtUri)
            .build()

        mediaSession.setMetadata(metadata)
    }

    /**
     * Met à jour l'état de lecture
     */
    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, exoPlayer.currentPosition, 1f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
            )
            .build()

        mediaSession.setPlaybackState(playbackState)
    }

    /**
     * Met à jour la notification
     */
    private fun updateNotification() {
        notificationManager.updateNotification(_currentTrack.value, _isPlaying.value)
    }

    /**
     * Démarre le service en foreground
     */
    private fun startForegroundService() {
        if (!isForegroundService) {
            val notification = notificationManager.buildNotification(
                _currentTrack.value,
                _isPlaying.value
            )
            startForeground(Constants.Player.NOTIFICATION_ID, notification)
            isForegroundService = true
        }
    }

    /**
     * Arrête le service foreground
     */
    private fun stopForegroundService() {
        if (isForegroundService) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
        }
    }

    /**
     * Démarre la mise à jour de la position
     */
    private fun startPositionUpdate() {
        serviceScope.launch {
            while (isActive && _isPlaying.value) {
                _currentPosition.value = exoPlayer.currentPosition
                delay(1000) // Mettre à jour chaque seconde
            }
        }
    }

    /**
     * Arrête la mise à jour de la position
     */
    private fun stopPositionUpdate() {
        // La coroutine s'arrêtera automatiquement car _isPlaying est false
    }

    /**
     * Crée une MediaDescription depuis une Track
     */
    private fun getMediaDescription(track: Track?): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setMediaId(track?.id?.toString() ?: "")
            .setTitle(track?.title ?: "")
            .setSubtitle(track?.artist ?: "")
            .setDescription(track?.album ?: "")
            .setIconUri(Uri.parse(track?.albumArtUri ?: ""))
            .build()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // Pour l'instant, retourner une liste vide
        // Peut être implémenté plus tard pour le browsing
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
        exoPlayer.release()
        mediaSession.isActive = false
        mediaSession.release()
        notificationManager.cleanup()

        stopForegroundService()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // Arrêter le service quand l'app est supprimée du récent
        if (!_isPlaying.value) {
            stopSelf()
        }
    }
}
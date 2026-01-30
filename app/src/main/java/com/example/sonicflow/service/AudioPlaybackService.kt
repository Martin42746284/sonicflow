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
import android.util.Log
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
import java.io.File
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
        private const val TAG = "AudioPlaybackService"
        private const val MEDIA_ROOT_ID = "root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    }

    // ✅ LocalBinder pour la connexion depuis MainActivity
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService {
            Log.d(TAG, "🔗 LocalBinder.getService() appelé")
            return this@AudioPlaybackService
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "🔗 onBind appelé")
        Log.d(TAG, "🔗 Intent: $intent")
        Log.d(TAG, "🔗 Action: ${intent?.action}")
        Log.d(TAG, "🔗 Package: ${intent?.`package`}")

        return if (intent?.action == "android.media.browse.MediaBrowserService") {
            Log.d(TAG, "🔗 Retour: super.onBind() pour MediaBrowserService")
            super.onBind(intent)
        } else {
            Log.d(TAG, "🔗 Retour: LocalBinder pour connexion directe")
            binder
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 onStartCommand appelé")
        Log.d(TAG, "🚀 Intent: $intent")
        Log.d(TAG, "🚀 Flags: $flags")
        Log.d(TAG, "🚀 StartId: $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🎬 Service onCreate appelé")
        Log.d(TAG, "🎬 Thread: ${Thread.currentThread().name}")

        try {
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
            Log.d(TAG, "✅ MediaSession créée")

            // Connecter ExoPlayer à MediaSession
            mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
                setPlayer(exoPlayer)
                setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                        return getMediaDescription(_currentTrack.value)
                    }
                })
            }

            Log.d(TAG, "✅ MediaSessionConnector configuré")

            // Créer le notification manager
            notificationManager = MediaNotificationManager(this, mediaSession)
            Log.d(TAG, "✅ NotificationManager créé")

            // Listener pour les changements d'état du player
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateName = when (playbackState) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    Log.d(TAG, "🔄 État de lecture changé: $stateName")

                    when (playbackState) {
                        Player.STATE_READY -> {
                            if (exoPlayer.playWhenReady) {
                                Log.d(TAG, "▶️ Lecture prête, démarrage du foreground service")
                                startForegroundService()
                            }
                        }
                        Player.STATE_ENDED -> {
                            Log.d(TAG, "⏭️ Piste terminée, passage à la suivante")
                            skipToNext()
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(TAG, "🎵 isPlaying changé: $isPlaying")
                    _isPlaying.value = isPlaying
                    updateNotification()

                    if (isPlaying) {
                        startPositionUpdate()
                    } else {
                        stopPositionUpdate()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "❌ Erreur de lecture: ${error.message}", error)
                    Log.e(TAG, "❌ Type d'erreur: ${error.errorCode}")
                    Log.e(TAG, "❌ Cause: ${error.cause}")
                    stopForegroundService()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    Log.d(TAG, "🎵 Transition vers: ${mediaItem?.mediaMetadata?.title}")
                }
            })

            // Callback pour les actions de la MediaSession
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.d(TAG, "▶️ MediaSession onPlay appelé")
                    exoPlayer.playWhenReady = true
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onPause() {
                    Log.d(TAG, "⏸️ MediaSession onPause appelé")
                    exoPlayer.playWhenReady = false
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onStop() {
                    Log.d(TAG, "⏹️ MediaSession onStop appelé")
                    stopSelf()
                }

                override fun onSkipToNext() {
                    Log.d(TAG, "⏭️ MediaSession onSkipToNext appelé")
                    skipToNext()
                }

                override fun onSkipToPrevious() {
                    Log.d(TAG, "⏮️ MediaSession onSkipToPrevious appelé")
                    skipToPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    Log.d(TAG, "⏩ MediaSession onSeekTo: $pos ms")
                    exoPlayer.seekTo(pos)
                    _currentPosition.value = pos
                }
            })

            Log.d(TAG, "✅ Service complètement initialisé")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation du service: ${e.message}", e)
        }
    }

    /**
     * ✅ Joue une piste avec vérification du fichier
     */
    fun playTrack(track: Track) {
        Log.d(TAG, "🎵 playTrack() appelé depuis l'extérieur")
        Log.d(TAG, "🎵 Track: ${track.title}")
        Log.d(TAG, "🎵 Thread: ${Thread.currentThread().name}")

        serviceScope.launch {
            try {
                Log.d(TAG, "🎵 Début de la coroutine playTrack")
                Log.d(TAG, "📂 Chemin: ${track.path}")

                _currentTrack.value = track

                // ✅ Vérifier que le fichier existe
                val file = File(track.path)
                if (!file.exists()) {
                    Log.e(TAG, "❌ Le fichier n'existe pas: ${track.path}")
                    return@launch
                }

                Log.d(TAG, "✅ Fichier trouvé, taille: ${file.length()} bytes")

                // ✅ Créer l'URI correctement
                val uri = Uri.fromFile(file)
                Log.d(TAG, "📍 URI créé: $uri")

                // Créer MediaItem depuis la piste
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(track.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .setArtworkUri(Uri.parse(track.albumArtUri ?: ""))
                            .build()
                    )
                    .build()

                Log.d(TAG, "📦 MediaItem créé")

                // Préparer et jouer
                exoPlayer.setMediaItem(mediaItem)
                Log.d(TAG, "📦 setMediaItem() appelé")

                exoPlayer.prepare()
                Log.d(TAG, "📦 prepare() appelé")

                exoPlayer.playWhenReady = true
                Log.d(TAG, "▶️ playWhenReady = true")

                updateMediaSessionMetadata(track)
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)

                Log.d(TAG, "✅ playTrack terminé avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception lors de playTrack: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * ✅ Pause la lecture
     */
    fun pause() {
        Log.d(TAG, "⏸️ pause() appelé")
        exoPlayer.playWhenReady = false
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
    }

    /**
     * ✅ Reprend la lecture
     */
    fun play() {
        Log.d(TAG, "▶️ play() appelé")
        exoPlayer.playWhenReady = true
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    /**
     * Met à jour la queue de lecture
     */
    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        Log.d(TAG, "📋 setQueue: ${tracks.size} pistes, index: $startIndex")

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(Uri.fromFile(File(track.path)))
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

        Log.d(TAG, "✅ Queue mise à jour")
    }

    /**
     * Passe à la piste suivante
     */
    private fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            Log.d(TAG, "⏭️ Passage à la piste suivante")
            exoPlayer.seekToNextMediaItem()
        } else {
            Log.d(TAG, "⚠️ Aucune piste suivante disponible")
        }
    }

    /**
     * Revient à la piste précédente
     */
    private fun skipToPrevious() {
        if (exoPlayer.currentPosition > Constants.Player.SEEK_BACK_THRESHOLD_MS) {
            Log.d(TAG, "⏮️ Retour au début de la piste")
            exoPlayer.seekTo(0)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            Log.d(TAG, "⏮️ Passage à la piste précédente")
            exoPlayer.seekToPreviousMediaItem()
        } else {
            Log.d(TAG, "⚠️ Aucune piste précédente disponible")
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
        Log.d(TAG, "✅ Métadonnées MediaSession mises à jour")
    }

    /**
     * Met à jour l'état de lecture
     */
    private fun updatePlaybackState(state: Int) {
        val stateName = when (state) {
            PlaybackStateCompat.STATE_PLAYING -> "PLAYING"
            PlaybackStateCompat.STATE_PAUSED -> "PAUSED"
            PlaybackStateCompat.STATE_STOPPED -> "STOPPED"
            else -> "OTHER"
        }
        Log.d(TAG, "🔄 updatePlaybackState: $stateName")

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
        if (isForegroundService) {
            notificationManager.updateNotification(_currentTrack.value, _isPlaying.value)
        }
    }

    /**
     * Démarre le service en foreground
     */
    private fun startForegroundService() {
        if (!isForegroundService) {
            Log.d(TAG, "🚀 Démarrage du service en foreground")
            val notification = notificationManager.buildNotification(
                _currentTrack.value,
                _isPlaying.value
            )
            startForeground(Constants.Player.NOTIFICATION_ID, notification)
            isForegroundService = true
            Log.d(TAG, "✅ Service en foreground démarré")
        }
    }

    /**
     * Arrête le service foreground
     */
    private fun stopForegroundService() {
        if (isForegroundService) {
            Log.d(TAG, "🛑 Arrêt du service foreground")
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
                delay(1000)
            }
        }
    }

    /**
     * Arrête la mise à jour de la position
     */
    private fun stopPositionUpdate() {
        // La coroutine s'arrêtera automatiquement
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
        Log.d(TAG, "📱 onGetRoot appelé par: $clientPackageName")
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.d(TAG, "📂 onLoadChildren appelé: $parentId")
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 Service onDestroy")

        serviceScope.cancel()
        exoPlayer.release()
        mediaSession.isActive = false
        mediaSession.release()
        notificationManager.cleanup()

        stopForegroundService()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 Tâche supprimée du récent")

        if (!_isPlaying.value) {
            stopSelf()
        }
    }
}

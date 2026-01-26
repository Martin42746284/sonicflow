package com.example.sonicflow.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.example.sonicflow.R
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

/**
 * Gère les notifications du lecteur audio
 */
class MediaNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        createNotificationChannel()
    }

    /**
     * Crée le canal de notification pour Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.Player.NOTIFICATION_CHANNEL_ID,
                Constants.Player.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for music playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Construit et retourne la notification
     */
    fun buildNotification(
        track: Track?,
        isPlaying: Boolean
    ): Notification {
        val builder = NotificationCompat.Builder(context, Constants.Player.NOTIFICATION_CHANNEL_ID)

        // Configurer le style media
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Previous, Play/Pause, Next
            .setShowCancelButton(true)

        builder.setStyle(mediaStyle)

        // Informations de base
        builder
            .setSmallIcon(R.drawable.ic_notification) // Assurez-vous d'avoir cette icône
            .setContentTitle(track?.title ?: "No track playing")
            .setContentText(track?.artist ?: "Unknown artist")
            .setSubText(track?.album)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)

        // Intent pour ouvrir l'app
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(contentIntent)

        // Actions
        builder.addAction(generateAction(NotificationAction.PREVIOUS))

        if (isPlaying) {
            builder.addAction(generateAction(NotificationAction.PAUSE))
        } else {
            builder.addAction(generateAction(NotificationAction.PLAY))
        }

        builder.addAction(generateAction(NotificationAction.NEXT))

        // Charger l'artwork de manière asynchrone
        track?.albumArtUri?.let { uri ->
            loadAlbumArt(uri) { bitmap ->
                builder.setLargeIcon(bitmap)
                notificationManager.notify(Constants.Player.NOTIFICATION_ID, builder.build())
            }
        }

        return builder.build()
    }

    /**
     * Génère une action pour la notification
     */
    private fun generateAction(action: NotificationAction): NotificationCompat.Action {
        val icon = when (action) {
            NotificationAction.PLAY -> android.R.drawable.ic_media_play
            NotificationAction.PAUSE -> android.R.drawable.ic_media_pause
            NotificationAction.PREVIOUS -> android.R.drawable.ic_media_previous
            NotificationAction.NEXT -> android.R.drawable.ic_media_next
        }

        val title = when (action) {
            NotificationAction.PLAY -> "Play"
            NotificationAction.PAUSE -> "Pause"
            NotificationAction.PREVIOUS -> "Previous"
            NotificationAction.NEXT -> "Next"
        }

        val actionCode = when (action) {
            NotificationAction.PLAY -> PlaybackStateCompat.ACTION_PLAY
            NotificationAction.PAUSE -> PlaybackStateCompat.ACTION_PAUSE
            NotificationAction.PREVIOUS -> PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            NotificationAction.NEXT -> PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }

        val intent = Intent(context, MediaButtonReceiver::class.java).apply {
            this.action = action.name
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            action.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    /**
     * Charge l'artwork de l'album de manière asynchrone
     */
    private fun loadAlbumArt(uri: String, onLoaded: (Bitmap?) -> Unit) {
        serviceScope.launch(Dispatchers.IO) {
            val bitmap = try {
                if (uri.startsWith("http")) {
                    // Charger depuis une URL
                    val url = URL(uri)
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } else {
                    // Charger depuis un fichier local
                    BitmapFactory.decodeFile(uri)
                }
            } catch (e: IOException) {
                null
            } catch (e: Exception) {
                null
            }

            launch(Dispatchers.Main) {
                onLoaded(bitmap)
            }
        }
    }

    /**
     * Met à jour la notification
     */
    fun updateNotification(track: Track?, isPlaying: Boolean) {
        val notification = buildNotification(track, isPlaying)
        notificationManager.notify(Constants.Player.NOTIFICATION_ID, notification)
    }

    /**
     * Annule la notification
     */
    fun cancelNotification() {
        notificationManager.cancel(Constants.Player.NOTIFICATION_ID)
    }

    /**
     * Nettoie les ressources
     */
    fun cleanup() {
        cancelNotification()
    }
}

/**
 * Actions disponibles dans la notification
 */
enum class NotificationAction {
    PLAY,
    PAUSE,
    PREVIOUS,
    NEXT
}

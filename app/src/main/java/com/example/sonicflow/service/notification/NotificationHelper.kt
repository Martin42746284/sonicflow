package com.example.sonicflow.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sonicflow.R
import com.example.sonicflow.presentation.util.Constants

/**
 * Helper pour créer différents types de notifications
 * Utilitaire général pour les notifications de l'app
 */
object NotificationHelper {

    private const val CHANNEL_ID_GENERAL = "sonicflow_general"
    private const val CHANNEL_NAME_GENERAL = "General Notifications"

    private const val CHANNEL_ID_DOWNLOAD = "sonicflow_download"
    private const val CHANNEL_NAME_DOWNLOAD = "Downloads"

    /**
     * Crée tous les canaux de notification nécessaires
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal pour les notifications générales
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                setShowBadge(true)
            }

            // Canal pour les téléchargements
            val downloadChannel = NotificationChannel(
                CHANNEL_ID_DOWNLOAD,
                CHANNEL_NAME_DOWNLOAD,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
                setShowBadge(false)
            }

            manager.createNotificationChannels(listOf(generalChannel, downloadChannel))
        }
    }

    /**
     * Affiche une notification simple
     */
    fun showSimpleNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Affiche une notification de progression
     */
    fun showProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        progress: Int,
        maxProgress: Int = 100
    ): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)

        return notification
    }

    /**
     * Met à jour une notification de progression
     */
    fun updateProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        progress: Int,
        maxProgress: Int = 100,
        isIndeterminate: Boolean = false
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setProgress(maxProgress, progress, isIndeterminate)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Complète une notification de progression
     */
    fun completeProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Annule une notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }

    /**
     * Annule toutes les notifications
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
    }
}

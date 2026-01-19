package com.example.sonicflow.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.sonicflow.service.AudioPlaybackService

/**
 * Receiver pour gérer les actions de la notification
 * Semaine 2, Jours 11-12
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val serviceIntent = Intent(context, AudioPlaybackService::class.java).apply {
            this.action = action
        }

        when (action) {
            NotificationAction.PLAY.name -> {
                serviceIntent.action = ACTION_PLAY
            }
            NotificationAction.PAUSE.name -> {
                serviceIntent.action = ACTION_PAUSE
            }
            NotificationAction.PREVIOUS.name -> {
                serviceIntent.action = ACTION_PREVIOUS
            }
            NotificationAction.NEXT.name -> {
                serviceIntent.action = ACTION_NEXT
            }
            ACTION_STOP -> {
                serviceIntent.action = ACTION_STOP
            }
        }

        context.startService(serviceIntent)
    }

    companion object {
        const val ACTION_PLAY = "com.example.sonicflow.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.sonicflow.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.example.sonicflow.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.sonicflow.ACTION_NEXT"
        const val ACTION_STOP = "com.example.sonicflow.ACTION_STOP"
    }
}

package com.example.sonicflow.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper pour gérer les permissions de l'application
 */
object PermissionHelper {

    /**
     * Permissions requises pour lire les fichiers audio
     */
    val AUDIO_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * Permission pour les notifications (Android 13+)
     */
    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    /**
     * Vérifie si toutes les permissions audio sont accordées
     */
    fun hasAudioPermissions(context: Context): Boolean {
        return AUDIO_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Vérifie si la permission de notification est accordée
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pas besoin sur les versions antérieures
        }
    }

    /**
     * Obtient toutes les permissions manquantes
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()

        AUDIO_PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED) {
                missing.add(permission)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        return missing
    }

    /**
     * Convertit un nom de permission en message lisible
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE ->
                "This permission is required to access and play your music files."

            Manifest.permission.POST_NOTIFICATIONS ->
                "This permission allows the app to show playback controls in notifications."

            else -> "This permission is required for the app to function properly."
        }
    }
}

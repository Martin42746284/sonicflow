package com.example.sonicflow

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sonicflow.presentation.navigation.NavGraph
import com.example.sonicflow.presentation.theme.SonicFlowTheme
import com.example.sonicflow.presentation.util.PermissionHelper
import com.example.sonicflow.service.notification.NotificationHelper
import com.example.sonicflow.service.AudioPlaybackService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principale de SonicFlow
 * Point d'entrée de l'application
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var audioService: AudioPlaybackService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? AudioPlaybackService.LocalBinder
            audioService = binder?.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    // Launcher pour demander les permissions audio
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions accordées, on peut continuer
            onPermissionsGranted()
        } else {
            // Permissions refusées
            onPermissionsDenied()
        }
    }

    // Launcher pour la permission de notification (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            // L'utilisateur a refusé les notifications
            // L'app peut fonctionner, mais sans notifications
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Installer le splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Activer edge-to-edge display
        enableEdgeToEdge()

        // Créer les canaux de notification
        NotificationHelper.createNotificationChannels(this)

        // Vérifier et demander les permissions
        checkAndRequestPermissions()

        // Configurer le contenu
        setContent {
            SonicFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Bind au service audio
        Intent(this, AudioPlaybackService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        // Unbind du service
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    /**
     * Vérifie et demande les permissions nécessaires
     */
    private fun checkAndRequestPermissions() {
        // Vérifier les permissions audio
        if (!PermissionHelper.hasAudioPermissions(this)) {
            audioPermissionLauncher.launch(PermissionHelper.AUDIO_PERMISSIONS)
        } else {
            onPermissionsGranted()
        }

        // Demander la permission de notification (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Appelé quand les permissions audio sont accordées
     */
    private fun onPermissionsGranted() {
        // Les permissions sont accordées, l'app peut fonctionner normalement
        // Le scan des fichiers audio sera déclenché depuis LibraryScreen
    }

    /**
     * Appelé quand les permissions audio sont refusées
     */
    private fun onPermissionsDenied() {
        // Afficher un message explicatif ou fermer l'app
        // Pour l'instant, on laisse l'app ouverte mais elle ne pourra pas accéder aux fichiers
    }

    override fun onDestroy() {
        super.onDestroy()

        // Nettoyer les ressources si nécessaire
        if (serviceBound) {
            unbindService(serviceConnection)
        }
    }
}

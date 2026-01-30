package com.example.sonicflow

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sonicflow.data.repository.PlayerRepositoryImpl
import com.example.sonicflow.presentation.navigation.NavGraph
import com.example.sonicflow.presentation.theme.SonicFlowTheme
import com.example.sonicflow.service.AudioPlaybackService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ✅ IMPORTANT : Injecter PlayerRepositoryImpl (pas PlayerRepository)
    @Inject
    lateinit var playerRepository: PlayerRepositoryImpl

    private var audioService: AudioPlaybackService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "🔗 onServiceConnected appelé")

            val localBinder = binder as? AudioPlaybackService.LocalBinder
            audioService = localBinder?.getService()

            if (audioService != null) {
                isBound = true
                Log.d(TAG, "✅ Service récupéré avec succès")

                // ✅ Connecter le service au repository
                playerRepository.setAudioService(audioService!!)
                Log.d(TAG, "✅ Service connecté au PlayerRepository")
            } else {
                Log.e(TAG, "❌ Impossible de récupérer le service (binder null)")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "❌ Service déconnecté")
            audioService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🎬 MainActivity onCreate")

        // ✅ Démarrer et binder le service
        startAndBindAudioService()

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

    private fun startAndBindAudioService() {
        Log.d(TAG, "🚀 Démarrage du service audio...")

        Intent(this, AudioPlaybackService::class.java).also { intent ->
            // Démarrer le service
            try {
                startService(intent)
                Log.d(TAG, "✅ startService() appelé")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur startService: ${e.message}", e)
            }

            // Binder le service
            try {
                val bound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "🔗 bindService() appelé, résultat: $bound")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur bindService: ${e.message}", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "▶️ MainActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ MainActivity onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 MainActivity onDestroy")

        if (isBound) {
            try {
                unbindService(serviceConnection)
                isBound = false
                Log.d(TAG, "✅ Service débindé")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors du unbind: ${e.message}", e)
            }
        }
    }
}
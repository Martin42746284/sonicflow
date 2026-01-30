package com.example.sonicflow.domain.usecase.player

import android.util.Log
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * Use case pour jouer une piste
 */
class PlayTrackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    companion object {
        private const val TAG = "PlayTrackUseCase"
    }

    suspend operator fun invoke(track: Track) {
        Log.d(TAG, "🎵 PlayTrackUseCase invoqué pour: ${track.title}")
        Log.d(TAG, "📂 Chemin: ${track.path}")

        try {
            playerRepository.playTrack(track)
            Log.d(TAG, "✅ PlayerRepository.playTrack() exécuté avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur dans PlayTrackUseCase: ${e.message}", e)
            throw e
        }
    }
}
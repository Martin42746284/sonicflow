package com.example.sonicflow.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.example.sonicflow.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {

    companion object {
        private const val TAG = "MediaRepository"
    }

    override suspend fun extractWaveform(audioPath: String, samplesCount: Int): List<Float> {
        Log.d(TAG, "🎵 Extraction waveform: $audioPath")

        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioPath)

            // Pour l'instant, retourner une waveform par défaut
            // TODO: Implémenter l'extraction réelle
            val defaultWaveform = List(samplesCount) {
                (0.1f + Math.random().toFloat() * 0.9f)
            }

            retriever.release()
            Log.d(TAG, "✅ Waveform générée: ${defaultWaveform.size} samples")
            defaultWaveform
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur extraction waveform: ${e.message}", e)
            // Retourner une waveform par défaut en cas d'erreur
            List(samplesCount) { 0.5f }
        }
    }
}
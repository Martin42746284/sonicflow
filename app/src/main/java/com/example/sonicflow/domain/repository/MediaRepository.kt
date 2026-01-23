package com.example.sonicflow.domain.repository

interface MediaRepository {
    /**
     * Génère les données de waveform pour un fichier audio
     */
    suspend fun generateWaveformData(audioPath: String, samplesCount: Int): String
}
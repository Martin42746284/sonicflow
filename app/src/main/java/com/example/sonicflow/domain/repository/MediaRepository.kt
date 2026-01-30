package com.example.sonicflow.domain.repository

interface MediaRepository {
    /**
     * Extrait la waveform d'un fichier audio
     * @param audioPath Chemin du fichier audio
     * @param samplesCount Nombre d'échantillons à extraire
     * @return Liste des amplitudes normalisées (0.0 à 1.0)
     */
    suspend fun extractWaveform(audioPath: String, samplesCount: Int): List<Float>
}
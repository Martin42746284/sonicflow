package com.example.sonicflow.domain.repository

interface MediaRepository {

    /**
     * Génère les données de waveform pour un fichier audio
     * @param audioPath Chemin du fichier audio
     * @param samplesCount Nombre d'échantillons à générer (par défaut 100)
     * @return JSON string contenant les amplitudes normalisées (0-1)
     */
    suspend fun generateWaveformData(
        audioPath: String,
        samplesCount: Int = 100
    ): String?

    /**
     * Extrait la durée d'un fichier audio
     * @param audioPath Chemin du fichier audio
     * @return Durée en millisecondes
     */
    suspend fun extractAudioDuration(audioPath: String): Long
}

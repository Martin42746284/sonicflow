package com.example.sonicflow.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long, // en millisecondes
    val path: String,
    val albumArtUri: String?,
    val dateAdded: Long, // timestamp
    val size: Long, // taille du fichier en bytes
    val mimeType: String?,
    val waveformData: String? = null
) {
    // Fonction utilitaire pour formater la durée
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Fonction pour obtenir la taille formatée
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.2f MB", size / (1024.0 * 1024.0))
        }
    }

    // Vérifie si la waveform est disponible
    fun hasWaveform(): Boolean = !waveformData.isNullOrEmpty()
}

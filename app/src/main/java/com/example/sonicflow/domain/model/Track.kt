package com.example.sonicflow.domain.model

data class Track(
    val id: Long = 0L,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,  // ✅ Propriété nécessaire
    val path: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val waveformData: String? = null
) {
    /**
     * Formatte la durée en mm:ss
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    /**
     * Vérifie si la piste a des données de waveform
     */
    fun hasWaveform(): Boolean {
        return !waveformData.isNullOrBlank()
    }
}
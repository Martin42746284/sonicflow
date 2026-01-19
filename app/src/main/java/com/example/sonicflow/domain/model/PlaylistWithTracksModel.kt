package com.example.sonicflow.domain.model

data class PlaylistWithTracksModel(
    val playlist: Playlist,
    val tracks: List<Track>
) {
    // Nombre de pistes dans la playlist
    val trackCount: Int
        get() = tracks.size

    // Durée totale de la playlist
    val totalDuration: Long
        get() = tracks.sumOf { it.duration }

    // Durée formatée
    fun getFormattedTotalDuration(): String {
        val totalSeconds = totalDuration / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%dh %02dm", hours, minutes)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }

    // Vérifie si la playlist est vide
    fun isEmpty(): Boolean = tracks.isEmpty()

    // Obtient la première piste (pour la lecture)
    fun getFirstTrack(): Track? = tracks.firstOrNull()

    // Vérifie si une piste est dans la playlist
    fun containsTrack(trackId: Long): Boolean = tracks.any { it.id == trackId }
}

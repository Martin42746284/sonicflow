package com.example.sonicflow.domain.model

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1
) {
    // Progression en pourcentage (0-100)
    val progress: Float
        get() = if (duration > 0) (currentPosition.toFloat() / duration * 100f) else 0f

    // Vérifie s'il y a une piste suivante
    fun hasNext(): Boolean = currentQueueIndex < queue.size - 1

    // Vérifie s'il y a une piste précédente
    fun hasPrevious(): Boolean = currentQueueIndex > 0

    // Obtient la piste suivante
    fun getNextTrack(): Track? {
        return if (hasNext()) queue[currentQueueIndex + 1] else null
    }

    // Obtient la piste précédente
    fun getPreviousTrack(): Track? {
        return if (hasPrevious()) queue[currentQueueIndex - 1] else null
    }

    // Formater la position actuelle
    fun getFormattedPosition(): String {
        val totalSeconds = currentPosition / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Formater la durée
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

enum class RepeatMode {
    OFF,    // Pas de répétition
    ONE,    // Répéter la piste actuelle
    ALL;    // Répéter toute la queue

    // Cycle vers le mode suivant
    fun next(): RepeatMode {
        return when (this) {
            OFF -> ONE
            ONE -> ALL
            ALL -> OFF
        }
    }
}

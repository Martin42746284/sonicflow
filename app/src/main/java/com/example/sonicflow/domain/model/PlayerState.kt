package com.example.sonicflow.domain.model

/**
 * État global du lecteur audio
 */
data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0f,  // ✅ Ajouter ce paramètre
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false
) {
    /**
     * Vérifie s'il y a une piste suivante
     */
    fun hasNext(): Boolean {
        return currentQueueIndex < queue.size - 1
    }

    /**
     * Vérifie s'il y a une piste précédente
     */
    fun hasPrevious(): Boolean {
        return currentQueueIndex > 0
    }

    /**
     * Récupère la piste suivante selon le mode de lecture
     */
    fun getNextTrack(): Track? {
        return when (repeatMode) {
            RepeatMode.OFF -> {
                if (hasNext()) queue[currentQueueIndex + 1] else null
            }
            RepeatMode.ONE -> currentTrack
            RepeatMode.ALL -> {
                if (hasNext()) {
                    queue[currentQueueIndex + 1]
                } else {
                    queue.firstOrNull()
                }
            }
        }
    }

    /**
     * Récupère la piste précédente
     */
    fun getPreviousTrack(): Track? {
        return if (hasPrevious()) {
            queue[currentQueueIndex - 1]
        } else if (repeatMode == RepeatMode.ALL) {
            queue.lastOrNull()
        } else {
            null
        }
    }

    /**
     * Formatte la position en mm:ss
     */
    fun getFormattedPosition(): String {
        val seconds = currentPosition / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    /**
     * Formatte la durée en mm:ss
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}
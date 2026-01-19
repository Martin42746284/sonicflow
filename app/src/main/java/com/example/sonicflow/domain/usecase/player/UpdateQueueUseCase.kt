package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.Track
import javax.inject.Inject

class UpdateQueueUseCase @Inject constructor() {
    /**
     * Met à jour la queue de lecture
     * @param newQueue Nouvelle liste de pistes
     * @param startIndex Index de départ (optionnel)
     * @return Pair(queue, index de départ)
     */
    operator fun invoke(
        newQueue: List<Track>,
        startIndex: Int = 0
    ): Pair<List<Track>, Int> {
        val validIndex = startIndex.coerceIn(0, maxOf(0, newQueue.size - 1))
        return Pair(newQueue, validIndex)
    }

    /**
     * Ajoute une piste à la queue
     */
    fun addToQueue(currentQueue: List<Track>, track: Track): List<Track> {
        return currentQueue + track
    }

    /**
     * Ajoute plusieurs pistes à la queue
     */
    fun addAllToQueue(currentQueue: List<Track>, tracks: List<Track>): List<Track> {
        return currentQueue + tracks
    }

    /**
     * Retire une piste de la queue
     */
    fun removeFromQueue(currentQueue: List<Track>, trackId: Long): List<Track> {
        return currentQueue.filter { it.id != trackId }
    }

    /**
     * Vide la queue
     */
    fun clearQueue(): List<Track> {
        return emptyList()
    }
}

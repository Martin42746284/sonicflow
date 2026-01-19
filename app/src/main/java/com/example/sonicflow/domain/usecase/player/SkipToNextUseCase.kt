package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.PlayerState
import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import javax.inject.Inject

class SkipToNextUseCase @Inject constructor() {
    /**
     * Passe à la piste suivante en fonction de l'état du lecteur
     * @param playerState État actuel du lecteur
     * @return La piste suivante à jouer, ou null s'il n'y en a pas
     */
    operator fun invoke(playerState: PlayerState): Track? {
        return when {
            // Si repeat ONE est activé, retourne la piste actuelle
            playerState.repeatMode == RepeatMode.ONE -> {
                playerState.currentTrack
            }

            // Si shuffle est activé, retourne une piste aléatoire
            playerState.shuffleEnabled && playerState.queue.isNotEmpty() -> {
                val remainingTracks = playerState.queue.filterIndexed { index, _ ->
                    index != playerState.currentQueueIndex
                }
                remainingTracks.randomOrNull() ?: playerState.queue.firstOrNull()
            }

            // Si on a une piste suivante dans la queue
            playerState.hasNext() -> {
                playerState.getNextTrack()
            }

            // Si repeat ALL est activé, retourne à la première piste
            playerState.repeatMode == RepeatMode.ALL && playerState.queue.isNotEmpty() -> {
                playerState.queue.first()
            }

            // Sinon, pas de piste suivante
            else -> null
        }
    }

    /**
     * Obtient l'index de la prochaine piste dans la queue
     */
    fun getNextIndex(playerState: PlayerState): Int? {
        return when {
            playerState.repeatMode == RepeatMode.ONE -> {
                playerState.currentQueueIndex
            }

            playerState.shuffleEnabled && playerState.queue.isNotEmpty() -> {
                val remainingIndices = playerState.queue.indices.filter { it != playerState.currentQueueIndex }
                remainingIndices.randomOrNull() ?: 0
            }

            playerState.hasNext() -> {
                playerState.currentQueueIndex + 1
            }

            playerState.repeatMode == RepeatMode.ALL && playerState.queue.isNotEmpty() -> {
                0
            }

            else -> null
        }
    }
}

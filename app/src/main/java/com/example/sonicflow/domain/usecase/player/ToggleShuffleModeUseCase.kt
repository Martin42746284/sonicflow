package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.Track
import javax.inject.Inject

class ToggleShuffleModeUseCase @Inject constructor() {
    /**
     * Active/désactive le mode shuffle
     * @param currentShuffleState État actuel du shuffle
     * @param queue Queue actuelle
     * @param currentTrack Piste actuellement jouée
     * @return Pair(nouveau shuffle state, nouvelle queue mélangée ou originale)
     */
    operator fun invoke(
        currentShuffleState: Boolean,
        queue: List<Track>,
        currentTrack: Track?
    ): Pair<Boolean, List<Track>> {
        val newShuffleState = !currentShuffleState

        return if (newShuffleState) {
            // Activer shuffle: mélanger la queue sauf la piste actuelle
            val shuffledQueue = if (currentTrack != null) {
                val otherTracks = queue.filter { it.id != currentTrack.id }.shuffled()
                listOf(currentTrack) + otherTracks
            } else {
                queue.shuffled()
            }
            Pair(true, shuffledQueue)
        } else {
            // Désactiver shuffle: retourner à l'ordre original
            // Note: L'ordre original devrait être conservé ailleurs dans l'app
            Pair(false, queue)
        }
    }
}

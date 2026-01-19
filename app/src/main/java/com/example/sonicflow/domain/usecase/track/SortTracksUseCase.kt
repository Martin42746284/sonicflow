package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SortTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Récupère les pistes triées selon l'ordre spécifié
     * @param sortOrder Ordre de tri (TITLE, ARTIST, DURATION, DATE_ADDED)
     * @return Flow de la liste triée
     */
    operator fun invoke(sortOrder: SortOrder): Flow<List<Track>> {
        return trackRepository.getTracksSorted(sortOrder)
    }

    /**
     * Trie par titre (A-Z)
     */
    fun sortByTitle(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.TITLE)
    }

    /**
     * Trie par artiste (A-Z)
     */
    fun sortByArtist(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.ARTIST)
    }

    /**
     * Trie par durée (plus long au plus court)
     */
    fun sortByDuration(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DURATION)
    }

    /**
     * Trie par date d'ajout (plus récent en premier)
     */
    fun sortByDateAdded(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DATE_ADDED)
    }
}

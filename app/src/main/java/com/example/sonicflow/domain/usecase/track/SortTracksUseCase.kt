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
     * @param sortOrder Ordre de tri
     * @return Flow de la liste triée
     */
    operator fun invoke(sortOrder: SortOrder): Flow<List<Track>> {
        return trackRepository.getTracksSorted(sortOrder)
    }

    /**
     * Trie par titre (A-Z)
     */
    fun sortByTitleAsc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.TITLE_ASC)
    }

    /**
     * Trie par titre (Z-A)
     */
    fun sortByTitleDesc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.TITLE_DESC)
    }

    /**
     * Trie par artiste (A-Z)
     */
    fun sortByArtistAsc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.ARTIST_ASC)
    }

    /**
     * Trie par artiste (Z-A)
     */
    fun sortByArtistDesc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.ARTIST_DESC)
    }

    /**
     * Trie par durée croissante (court → long)
     */
    fun sortByDurationAsc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DURATION_ASC)
    }

    /**
     * Trie par durée décroissante (long → court)
     */
    fun sortByDurationDesc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DURATION_DESC)
    }

    /**
     * Trie par date d'ajout (plus ancien en premier)
     */
    fun sortByDateAddedAsc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DATE_ADDED_ASC)
    }

    /**
     * Trie par date d'ajout (plus récent en premier)
     */
    fun sortByDateAddedDesc(): Flow<List<Track>> {
        return trackRepository.getTracksSorted(SortOrder.DATE_ADDED_DESC)
    }

    /**
     * Inverse l'ordre de tri actuel
     */
    fun toggleSortOrder(currentOrder: SortOrder): SortOrder {
        return when (currentOrder) {
            SortOrder.TITLE_ASC -> SortOrder.TITLE_DESC
            SortOrder.TITLE_DESC -> SortOrder.TITLE_ASC
            SortOrder.ARTIST_ASC -> SortOrder.ARTIST_DESC
            SortOrder.ARTIST_DESC -> SortOrder.ARTIST_ASC
            SortOrder.DURATION_ASC -> SortOrder.DURATION_DESC
            SortOrder.DURATION_DESC -> SortOrder.DURATION_ASC
            SortOrder.DATE_ADDED_ASC -> SortOrder.DATE_ADDED_DESC
            SortOrder.DATE_ADDED_DESC -> SortOrder.DATE_ADDED_ASC
        }
    }
}
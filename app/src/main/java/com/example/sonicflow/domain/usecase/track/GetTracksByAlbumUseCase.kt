package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTracksByAlbumUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Récupère toutes les pistes d'un album
     * @param albumName Nom de l'album
     */
    operator fun invoke(albumName: String): Flow<List<Track>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.filter { it.album?.equals(albumName, ignoreCase = true) == true }
        }
    }

    /**
     * Récupère tous les albums uniques
     */
    fun getAllAlbums(): Flow<List<String>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.mapNotNull { it.album }
                .distinct()
                .sorted()
        }
    }
}

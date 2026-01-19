package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTracksByArtistUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Récupère toutes les pistes d'un artiste
     * @param artistName Nom de l'artiste
     */
    operator fun invoke(artistName: String): Flow<List<Track>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.filter { it.artist.equals(artistName, ignoreCase = true) }
        }
    }

    /**
     * Récupère tous les artistes uniques
     */
    fun getAllArtists(): Flow<List<String>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.map { it.artist }
                .distinct()
                .sorted()
        }
    }

    /**
     * Récupère les artistes avec le nombre de pistes
     */
    fun getArtistsWithTrackCount(): Flow<Map<String, Int>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.groupBy { it.artist }
                .mapValues { it.value.size }
        }
    }
}

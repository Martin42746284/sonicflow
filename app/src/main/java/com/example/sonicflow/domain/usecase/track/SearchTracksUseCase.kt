package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Recherche des pistes par titre ou artiste
     * @param query Terme de recherche
     * @return Flow de la liste filtrée
     */
    operator fun invoke(query: String): Flow<List<Track>> {
        // Si la requête est vide, retourner toutes les pistes
        if (query.isBlank()) {
            return trackRepository.getAllTracks()
        }

        return trackRepository.searchTracks(query.trim())
    }

    /**
     * Recherche avec filtres avancés
     * @param query Terme de recherche
     * @param searchInTitle Rechercher dans les titres
     * @param searchInArtist Rechercher dans les artistes
     * @param searchInAlbum Rechercher dans les albums
     * @param minDuration Durée minimale en millisecondes
     * @param maxDuration Durée maximale en millisecondes
     */
    fun searchWithFilters(
        query: String,
        searchInTitle: Boolean = true,
        searchInArtist: Boolean = true,
        searchInAlbum: Boolean = true,
        minDuration: Long? = null,
        maxDuration: Long? = null
    ): Flow<List<Track>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.filter { track ->
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    val lowerQuery = query.lowercase()
                    (searchInTitle && track.title.lowercase().contains(lowerQuery)) ||
                            (searchInArtist && track.artist.lowercase().contains(lowerQuery)) ||
                            (searchInAlbum && track.album?.lowercase()?.contains(lowerQuery) == true)
                }

                val matchesDuration = when {
                    minDuration != null && maxDuration != null ->
                        track.duration in minDuration..maxDuration
                    minDuration != null -> track.duration >= minDuration
                    maxDuration != null -> track.duration <= maxDuration
                    else -> true
                }

                matchesQuery && matchesDuration
            }
        }
    }

    /**
     * Recherche par artiste exact
     */
    fun searchByArtist(artistName: String): Flow<List<Track>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.filter { it.artist.equals(artistName, ignoreCase = true) }
        }
    }

    /**
     * Recherche par album exact
     */
    fun searchByAlbum(albumName: String): Flow<List<Track>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.filter { it.album?.equals(albumName, ignoreCase = true) == true }
        }
    }
}

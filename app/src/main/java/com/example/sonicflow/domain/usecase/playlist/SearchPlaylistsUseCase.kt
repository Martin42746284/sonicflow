package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Recherche des playlists par nom
     * @param query Terme de recherche
     * @return Flow de la liste filtrée
     */
    operator fun invoke(query: String): Flow<List<Playlist>> {
        // Si la requête est vide, retourner toutes les playlists
        if (query.isBlank()) {
            return playlistRepository.getAllPlaylists()
        }

        return playlistRepository.searchPlaylists(query.trim())
    }

    /**
     * Recherche avec filtres supplémentaires
     */
    fun searchWithFilters(
        query: String,
        minTrackCount: Int? = null,
        sortByName: Boolean = false
    ): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists().map { playlists ->
            var filtered = playlists

            // Filtrer par recherche
            if (query.isNotBlank()) {
                filtered = filtered.filter { playlist ->
                    playlist.name.contains(query, ignoreCase = true) ||
                            playlist.description?.contains(query, ignoreCase = true) == true
                }
            }

            // Filtrer par nombre minimum de pistes
            minTrackCount?.let { minCount ->
                // Note: Cette partie nécessiterait d'avoir accès au nombre de pistes
                // On pourrait utiliser getAllPlaylistsWithTracks() à la place
            }

            // Trier par nom si demandé
            if (sortByName) {
                filtered = filtered.sortedBy { it.name.lowercase() }
            }

            filtered
        }
    }
}

package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.data.local.entities.PlaylistWithTracks
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case pour récupérer les playlists
 * Semaine 3, Jours 18-19
 */
class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Récupère toutes les playlists
     */
    operator fun invoke(): Flow<List<com.example.sonicflow.domain.model.Playlist>> {
        return playlistRepository.getAllPlaylists()
    }

    /**
     * Récupère une playlist avec ses tracks
     */
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks> {
        return playlistRepository.getPlaylistWithTracks(playlistId)
    }
}
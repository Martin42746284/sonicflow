package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracksModel
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Récupère toutes les playlists
     */
    operator fun invoke(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }

    /**
     * Récupère toutes les playlists avec leurs pistes
     */
    fun getPlaylistsWithTracks(): Flow<List<PlaylistWithTracksModel>> {
        return playlistRepository.getAllPlaylistsWithTracks()
    }

    /**
     * Récupère une playlist spécifique par ID
     */
    suspend fun getPlaylistById(playlistId: Long): Result<Playlist> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist != null) {
                Result.success(playlist)
            } else {
                Result.failure(Exception("Playlist not found with id: $playlistId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère une playlist avec ses pistes
     */
    suspend fun getPlaylistWithTracks(playlistId: Long): Result<PlaylistWithTracksModel> {
        return try {
            val playlist = playlistRepository.getPlaylistWithTracks(playlistId)
            if (playlist != null) {
                Result.success(playlist)
            } else {
                Result.failure(Exception("Playlist not found with id: $playlistId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

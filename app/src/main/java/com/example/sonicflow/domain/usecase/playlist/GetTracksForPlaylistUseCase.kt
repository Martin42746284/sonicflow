package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetTracksForPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Récupère toutes les pistes d'une playlist
     * @param playlistId ID de la playlist
     * @return Flow de la liste des pistes
     */
    operator fun invoke(playlistId: Long): Flow<List<Track>> {
        return playlistRepository.getTracksForPlaylist(playlistId)
    }

    /**
     * Récupère le nombre de pistes dans une playlist
     */
    suspend fun getTrackCount(playlistId: Long): Result<Int> {
        return try {
            val tracks = playlistRepository.getTracksForPlaylist(playlistId).firstOrNull()
            val count = tracks?.size ?: 0
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Vérifie si une piste est dans une playlist
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Result<Boolean> {
        return try {
            val exists = playlistRepository.isTrackInPlaylist(playlistId, trackId)
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
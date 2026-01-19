package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveTrackFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Retire une piste d'une playlist
     * @param playlistId ID de la playlist
     * @param trackId ID de la piste
     */
    suspend operator fun invoke(
        playlistId: Long,
        trackId: Long
    ): Result<Unit> {
        return try {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retire toutes les pistes d'une playlist
     * @param playlistId ID de la playlist
     */
    suspend fun removeAllTracks(playlistId: Long): Result<Unit> {
        return try {
            playlistRepository.removeAllTracksFromPlaylist(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

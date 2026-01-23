package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddTrackToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Ajoute une piste à une playlist
     * @param playlistId ID de la playlist
     * @param trackId ID de la piste
     * @return Success ou erreur
     */
    suspend operator fun invoke(
        playlistId: Long,
        trackId: Long
    ): Result<Unit> {
        return try {
            playlistRepository.addTrackToPlaylist(playlistId, trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ajoute plusieurs pistes à une playlist
     * @param playlistId ID de la playlist
     * @param trackIds Liste des IDs des pistes
     * @return Nombre de pistes ajoutées
     */
    suspend fun addMultipleTracks(
        playlistId: Long,
        trackIds: List<Long>
    ): Result<Int> {
        return try {
            trackIds.forEach { trackId ->
                playlistRepository.addTrackToPlaylist(playlistId, trackId)
            }
            Result.success(trackIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
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
            // Vérifier si la piste existe déjà dans la playlist
            val alreadyExists = playlistRepository.isTrackInPlaylist(playlistId, trackId)

            if (alreadyExists) {
                return Result.failure(Exception("Track already exists in this playlist"))
            }

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
            var addedCount = 0

            trackIds.forEach { trackId ->
                val alreadyExists = playlistRepository.isTrackInPlaylist(playlistId, trackId)
                if (!alreadyExists) {
                    playlistRepository.addTrackToPlaylist(playlistId, trackId)
                    addedCount++
                }
            }

            Result.success(addedCount)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

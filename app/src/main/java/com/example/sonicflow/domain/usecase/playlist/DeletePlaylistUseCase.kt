package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class DeletePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Supprime une playlist
     * @param playlistId ID de la playlist à supprimer
     */
    suspend operator fun invoke(playlistId: Long): Result<Unit> {
        return try {
            playlistRepository.deletePlaylistById(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Supprime plusieurs playlists
     * @param playlistIds Liste des IDs des playlists à supprimer
     */
    suspend fun deleteMultiple(playlistIds: List<Long>): Result<Int> {
        return try {
            var deletedCount = 0
            playlistIds.forEach { playlistId ->
                playlistRepository.deletePlaylistById(playlistId)
                deletedCount++
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

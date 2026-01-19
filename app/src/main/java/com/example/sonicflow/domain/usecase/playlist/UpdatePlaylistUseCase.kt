package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * Met à jour une playlist
     * @param playlist Playlist avec les modifications
     */
    suspend operator fun invoke(playlist: Playlist): Result<Unit> {
        return try {
            // Validation
            if (playlist.name.isBlank()) {
                return Result.failure(Exception("Playlist name cannot be empty"))
            }

            if (playlist.name.length > 100) {
                return Result.failure(Exception("Playlist name is too long (max 100 characters)"))
            }

            playlistRepository.updatePlaylist(playlist)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour le nom d'une playlist
     */
    suspend fun updateName(playlistId: Long, newName: String): Result<Unit> {
        return try {
            if (newName.isBlank()) {
                return Result.failure(Exception("Playlist name cannot be empty"))
            }

            val playlist = playlistRepository.getPlaylistById(playlistId)
                ?: return Result.failure(Exception("Playlist not found"))

            val updatedPlaylist = playlist.update(name = newName)
            playlistRepository.updatePlaylist(updatedPlaylist)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour la description d'une playlist
     */
    suspend fun updateDescription(playlistId: Long, newDescription: String?): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
                ?: return Result.failure(Exception("Playlist not found"))

            val updatedPlaylist = playlist.update(description = newDescription)
            playlistRepository.updatePlaylist(updatedPlaylist)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour l'image de couverture
     */
    suspend fun updateCoverImage(playlistId: Long, imagePath: String?): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
                ?: return Result.failure(Exception("Playlist not found"))

            val updatedPlaylist = playlist.update(coverImagePath = imagePath)
            playlistRepository.updatePlaylist(updatedPlaylist)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

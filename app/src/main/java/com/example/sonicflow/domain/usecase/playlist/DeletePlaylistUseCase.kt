package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case pour supprimer une playlist
 */
class DeletePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }
}
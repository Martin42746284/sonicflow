package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case pour créer une playlist
 */
class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String?): Long {
        return playlistRepository.createPlaylist(name, description)
    }
}
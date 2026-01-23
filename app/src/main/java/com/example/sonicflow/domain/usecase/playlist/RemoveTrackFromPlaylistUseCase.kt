package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveTrackFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }
}
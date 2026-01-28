package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.data.repository.PlayerRepository
import javax.inject.Inject

/**
 * Use case pour jouer une piste
 */
class PlayTrackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(track: Track) {
        playerRepository.playTrack(track)
    }
}
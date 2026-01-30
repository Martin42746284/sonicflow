package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * Use case pour mettre en pause la lecture
 */
class PauseTrackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke() {
        playerRepository.pauseTrack()
    }
}
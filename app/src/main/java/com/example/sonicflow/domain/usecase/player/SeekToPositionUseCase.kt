package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.repository.PlayerRepository
import javax.inject.Inject

class SeekToPositionUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(position: Long) {
        playerRepository.seekTo(position)
    }
}
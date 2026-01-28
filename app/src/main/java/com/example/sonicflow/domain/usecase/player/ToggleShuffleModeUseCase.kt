package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.data.repository.PlayerRepository
import javax.inject.Inject

class ToggleShuffleModeUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke() {
        playerRepository.toggleShuffleMode()
    }
}
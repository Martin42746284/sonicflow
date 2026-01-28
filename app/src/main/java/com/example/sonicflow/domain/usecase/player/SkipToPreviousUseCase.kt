package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.data.repository.PlayerRepository
import javax.inject.Inject

/**
 * Use case pour passer à la piste précédente
 */
class SkipToPreviousUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke() {
        playerRepository.skipToPrevious()
    }
}
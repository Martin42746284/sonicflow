package com.example.sonicflow.domain.usecase.player

import javax.inject.Inject

class PauseTrackUseCase @Inject constructor() {
    /**
     * Met en pause la lecture actuelle
     * @param currentPosition Position actuelle de la lecture en millisecondes
     * @return La position sauvegardée
     */
    operator fun invoke(currentPosition: Long): Long {
        return currentPosition
    }
}

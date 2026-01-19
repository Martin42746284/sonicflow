package com.example.sonicflow.domain.usecase.player

import javax.inject.Inject

class SeekToPositionUseCase @Inject constructor() {
    /**
     * Déplace la position de lecture
     * @param position Position cible en millisecondes
     * @param duration Durée totale de la piste
     * @return Position valide (entre 0 et duration)
     */
    operator fun invoke(position: Long, duration: Long): Long {
        return position.coerceIn(0L, duration)
    }

    /**
     * Déplace la position de lecture par pourcentage
     * @param percentage Pourcentage (0.0 - 1.0)
     * @param duration Durée totale de la piste
     * @return Position en millisecondes
     */
    fun seekByPercentage(percentage: Float, duration: Long): Long {
        val clampedPercentage = percentage.coerceIn(0f, 1f)
        return (duration * clampedPercentage).toLong()
    }
}

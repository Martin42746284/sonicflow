package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import javax.inject.Inject

class PlayTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Prépare et lance la lecture d'une piste
     * @param trackId ID de la piste à jouer
     * @param queue Liste optionnelle des pistes en file d'attente
     * @return La piste à jouer, ou null si introuvable
     */
    suspend operator fun invoke(
        trackId: Long,
        queue: List<Track>? = null
    ): Result<Track> {
        return try {
            val track = trackRepository.getTrackById(trackId)
            if (track != null) {
                Result.success(track)
            } else {
                Result.failure(Exception("Track not found with id: $trackId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lance la lecture d'une piste directement
     */
    suspend operator fun invoke(track: Track): Result<Track> {
        return Result.success(track)
    }
}

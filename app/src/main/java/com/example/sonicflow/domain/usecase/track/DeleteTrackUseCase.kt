package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import javax.inject.Inject

class DeleteTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Supprime une piste de la base de données
     * Note: Cela ne supprime pas le fichier physique
     * @param track Piste à supprimer
     */
    suspend operator fun invoke(track: Track): Result<Unit> {
        return try {
            trackRepository.deleteTrack(track)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Supprime une piste par son ID
     */
    suspend fun deleteById(trackId: Long): Result<Unit> {
        return try {
            val track = trackRepository.getTrackById(trackId)
            if (track != null) {
                trackRepository.deleteTrack(track)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Track not found with id: $trackId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

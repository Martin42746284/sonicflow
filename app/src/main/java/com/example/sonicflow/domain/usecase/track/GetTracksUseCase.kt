package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Récupère toutes les pistes audio
     * @return Flow de la liste des pistes
     */
    operator fun invoke(): Flow<List<Track>> {
        return trackRepository.getAllTracks()
    }

    /**
     * Récupère une piste spécifique par ID
     * @param trackId ID de la piste
     */
    suspend fun getTrackById(trackId: Long): Result<Track> {
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
     * Récupère le nombre total de pistes
     */
    suspend fun getTracksCount(): Result<Int> {
        return try {
            val count = trackRepository.getTracksCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

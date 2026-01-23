package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import javax.inject.Inject

class UpdateTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Met à jour une piste
     * @param track Piste avec les modifications
     */
    suspend operator fun invoke(track: Track): Result<Unit> {
        return try {
            trackRepository.updateTrack(track)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour les données de waveform d'une piste
     * @param trackId ID de la piste
     * @param waveformData Données JSON de la waveform
     */
    suspend fun updateWaveformData(trackId: Long, waveformData: String): Result<Unit> {
        return try {
            trackRepository.updateWaveformData(trackId, waveformData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
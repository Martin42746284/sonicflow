package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.repository.TrackRepository
import javax.inject.Inject

class SyncTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * ✅ Scanner et synchroniser les pistes depuis MediaStore
     */
    suspend operator fun invoke() {
        trackRepository.syncTracks()
    }

    /**
     * ✅ Vérifier si un scan est nécessaire (base de données vide)
     */
    suspend fun isSyncNeeded(): Boolean {
        return trackRepository.getTracksCount() == 0
    }
}
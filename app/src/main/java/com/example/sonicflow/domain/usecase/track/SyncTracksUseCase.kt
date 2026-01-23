package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.repository.TrackRepository
import javax.inject.Inject

class SyncTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    /**
     * Synchronise les pistes depuis le MediaStore du téléphone
     * Scanne tous les fichiers audio et les ajoute à la base de données
     * @return Nombre de pistes synchronisées
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            trackRepository.syncTracksFromDevice()  // ✅ Correction : syncTracksFromDevice au lieu de syncTracksFromMediaStore
            val count = trackRepository.getTracksCount()
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(Exception("Permission denied. Please grant storage permission."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Vérifie si une synchronisation est nécessaire
     * Compare le nombre de pistes en base avec le MediaStore
     */
    suspend fun isSyncNeeded(): Boolean {
        return try {
            val dbCount = trackRepository.getTracksCount()
            // Si la base est vide, sync nécessaire
            dbCount == 0
        } catch (e: Exception) {
            true // En cas d'erreur, on suggère une sync
        }
    }
}
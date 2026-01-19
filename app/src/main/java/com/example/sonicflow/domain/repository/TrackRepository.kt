package com.example.sonicflow.domain.repository

import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    /**
     * Récupère toutes les pistes audio
     */
    fun getAllTracks(): Flow<List<Track>>

    /**
     * Récupère une piste par son ID
     */
    suspend fun getTrackById(trackId: Long): Track?

    /**
     * Recherche des pistes par titre ou artiste
     */
    fun searchTracks(query: String): Flow<List<Track>>

    /**
     * Récupère les pistes triées selon l'ordre spécifié
     */
    fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>>

    /**
     * Insère une nouvelle piste
     * @return l'ID de la piste insérée
     */
    suspend fun insertTrack(track: Track): Long

    /**
     * Met à jour une piste existante
     */
    suspend fun updateTrack(track: Track)

    /**
     * Supprime une piste
     */
    suspend fun deleteTrack(track: Track)

    /**
     * Synchronise les pistes depuis le MediaStore
     * Scanne le téléphone pour détecter tous les fichiers audio
     */
    suspend fun syncTracksFromMediaStore()

    /**
     * Met à jour les données de waveform pour une piste
     */
    suspend fun updateWaveformData(trackId: Long, waveformData: String)

    /**
     * Récupère le nombre total de pistes
     */
    suspend fun getTracksCount(): Int
}
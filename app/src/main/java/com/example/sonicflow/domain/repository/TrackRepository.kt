package com.example.sonicflow.domain.repository

import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gérer les pistes audio
 * Interface du Domain Layer (Clean Architecture)
 */
interface TrackRepository {

    /**
     * Récupérer toutes les pistes
     */
    fun getAllTracks(): Flow<List<Track>>

    /**
     * Récupérer une piste par son ID
     */
    suspend fun getTrackById(id: Long): Track?

    /**
     * ✅ Récupérer une piste par son ID (Flow)
     */
    fun getTrackByIdFlow(id: Long): Flow<Track?>

    /**
     * Insérer une nouvelle piste
     */
    suspend fun insertTrack(track: Track)

    /**
     * Mettre à jour une piste existante
     */
    suspend fun updateTrack(track: Track)

    /**
     * Supprimer une piste
     */
    suspend fun deleteTrack(track: Track)

    /**
     * Supprimer une piste par son ID
     */
    suspend fun deleteTrackById(trackId: Long)

    /**
     * Mettre à jour les données de forme d'onde (waveform)
     */
    suspend fun updateWaveformData(trackId: Long, waveformData: String)

    /**
     * ✅ Synchroniser les pistes depuis le MediaStore
     * Cette méthode scanne les fichiers audio du téléphone
     */
    suspend fun syncTracks()

    /**
     * @Deprecated Utiliser syncTracks() à la place
     */
    @Deprecated("Utiliser syncTracks()", ReplaceWith("syncTracks()"))
    suspend fun syncTracksFromDevice() {
        syncTracks()
    }

    /**
     * Rechercher des pistes par titre, artiste ou album
     */
    fun searchTracks(query: String): Flow<List<Track>>

    /**
     * ✅ Rechercher des pistes (suspend)
     */
    suspend fun searchTracksAsync(query: String): List<Track>

    /**
     * Récupérer toutes les pistes d'un album
     */
    fun getTracksByAlbum(album: String): Flow<List<Track>>

    /**
     * Récupérer toutes les pistes d'un artiste
     */
    fun getTracksByArtist(artist: String): Flow<List<Track>>

    /**
     * ✅ Récupérer des pistes par leurs IDs
     */
    suspend fun getTracksByIds(ids: List<Long>): List<Track>

    /**
     * Compter le nombre total de pistes
     */
    suspend fun getTracksCount(): Int

    /**
     * ✅ Récupérer les pistes triées selon un ordre spécifique
     */
    fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>>

    /**
     * ✅ Supprimer toutes les pistes (utile pour la synchro)
     */
    suspend fun deleteAllTracks()

    /**
     * ✅ Récupérer tous les albums distincts
     */
    fun getAllAlbums(): Flow<List<String>>

    /**
     * ✅ Récupérer tous les artistes distincts
     */
    fun getAllArtists(): Flow<List<String>>

    /**
     * ✅ Vérifier si une piste existe par son chemin
     */
    suspend fun trackExistsByPath(path: String): Boolean

    /**
     * ✅ Mettre à jour le nombre de lectures (play count)
     */
    suspend fun incrementPlayCount(trackId: Long)

    /**
     * ✅ Mettre à jour la dernière date de lecture
     */
    suspend fun updateLastPlayedDate(trackId: Long, timestamp: Long)
}
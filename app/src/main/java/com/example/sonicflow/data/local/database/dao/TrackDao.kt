package com.example.sonicflow.data.local.database.dao

import androidx.room.*
import com.example.sonicflow.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    // ==================== Queries de base ====================

    /**
     * Récupérer toutes les pistes (Flow)
     */
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    /**
     * Récupérer une piste par ID (Flow)
     */
    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: Long): Flow<TrackEntity?>

    /**
     * ✅ Récupérer une piste par ID (Suspend)
     */
    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackByIdSuspend(id: Long): TrackEntity?

    /**
     * Compter le nombre total de pistes
     */
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTracksCount(): Int

    // ==================== Recherche ====================

    /**
     * Rechercher des pistes par titre, artiste ou album (Suspend)
     */
    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query OR album LIKE :query ORDER BY title ASC")
    suspend fun searchTracks(query: String): List<TrackEntity>

    /**
     * ✅ Rechercher des pistes par titre, artiste ou album (Flow)
     */
    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query OR album LIKE :query ORDER BY title ASC")
    fun searchTracksFlow(query: String): Flow<List<TrackEntity>>

    // ==================== Filtres ====================

    /**
     * Récupérer les pistes par album
     */
    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>

    /**
     * Récupérer les pistes par artiste
     */
    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY title ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>

    /**
     * Récupérer les pistes par IDs
     */
    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    suspend fun getTracksByIds(ids: List<Long>): List<TrackEntity>

    /**
     * ✅ Vérifier si une piste existe par son chemin
     */
    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE data = :path LIMIT 1)")
    suspend fun trackExistsByPath(path: String): Boolean

    // ==================== Tri ====================

    /**
     * ✅ Trier les pistes par titre
     */
    @Query("SELECT * FROM tracks ORDER BY CASE WHEN :ascending = 1 THEN title END ASC, CASE WHEN :ascending = 0 THEN title END DESC")
    fun getTracksSortedByTitle(ascending: Boolean): Flow<List<TrackEntity>>

    /**
     * ✅ Trier les pistes par artiste
     */
    @Query("SELECT * FROM tracks ORDER BY CASE WHEN :ascending = 1 THEN artist END ASC, CASE WHEN :ascending = 0 THEN artist END DESC, title ASC")
    fun getTracksSortedByArtist(ascending: Boolean): Flow<List<TrackEntity>>

    /**
     * ✅ Trier les pistes par durée
     */
    @Query("SELECT * FROM tracks ORDER BY CASE WHEN :ascending = 1 THEN duration END ASC, CASE WHEN :ascending = 0 THEN duration END DESC")
    fun getTracksSortedByDuration(ascending: Boolean): Flow<List<TrackEntity>>

    /**
     * ✅ Trier les pistes par date d'ajout
     */
    @Query("SELECT * FROM tracks ORDER BY CASE WHEN :ascending = 1 THEN dateAdded END ASC, CASE WHEN :ascending = 0 THEN dateAdded END DESC")
    fun getTracksSortedByDateAdded(ascending: Boolean): Flow<List<TrackEntity>>

    // ==================== Agrégation ====================

    /**
     * ✅ Récupérer tous les albums distincts
     */
    @Query("SELECT DISTINCT album FROM tracks WHERE album IS NOT NULL AND album != '' ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>

    /**
     * ✅ Récupérer tous les artistes distincts
     */
    @Query("SELECT DISTINCT artist FROM tracks WHERE artist IS NOT NULL AND artist != '' ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>

    /**
     * ✅ Compter les pistes par artiste
     */
    @Query("SELECT COUNT(*) FROM tracks WHERE artist = :artist")
    suspend fun getTrackCountByArtist(artist: String): Int

    /**
     * ✅ Compter les pistes par album
     */
    @Query("SELECT COUNT(*) FROM tracks WHERE album = :album")
    suspend fun getTrackCountByAlbum(album: String): Int

    // ==================== Insertion ====================

    /**
     * Insérer une piste
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    /**
     * Insérer plusieurs pistes à la fois
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    // ==================== Mise à jour ====================

    /**
     * ✅ Mettre à jour une piste
     */
    @Update
    suspend fun updateTrack(track: TrackEntity)

    /**
     * ✅ Mettre à jour les données de forme d'onde
     */
    @Query("UPDATE tracks SET waveformData = :waveformData WHERE id = :trackId")
    suspend fun updateWaveformData(trackId: Long, waveformData: String)

    /**
     * ✅ Incrémenter le compteur de lectures
     */
    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: Long)

    /**
     * ✅ Mettre à jour la dernière date de lecture
     */
    @Query("UPDATE tracks SET lastPlayedDate = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayedDate(trackId: Long, timestamp: Long)

    // ==================== Suppression ====================

    /**
     * Supprimer une piste
     */
    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    /**
     * ✅ Supprimer une piste par ID
     */
    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: Long)

    /**
     * Supprimer toutes les pistes
     */
    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    /**
     * ✅ Supprimer les pistes par artiste
     */
    @Query("DELETE FROM tracks WHERE artist = :artist")
    suspend fun deleteTracksByArtist(artist: String)

    /**
     * ✅ Supprimer les pistes par album
     */
    @Query("DELETE FROM tracks WHERE album = :album")
    suspend fun deleteTracksByAlbum(album: String)

    // ==================== Statistiques ====================

    /**
     * ✅ Récupérer la durée totale de toutes les pistes
     */
    @Query("SELECT SUM(duration) FROM tracks")
    suspend fun getTotalDuration(): Long?

    /**
     * ✅ Récupérer les pistes les plus jouées
     */
    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedTracks(limit: Int = 10): Flow<List<TrackEntity>>

    /**
     * ✅ Récupérer les pistes récemment jouées
     */
    @Query("SELECT * FROM tracks WHERE lastPlayedDate IS NOT NULL ORDER BY lastPlayedDate DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int = 10): Flow<List<TrackEntity>>

    /**
     * ✅ Récupérer les pistes récemment ajoutées
     */
    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedTracks(limit: Int = 10): Flow<List<TrackEntity>>
}

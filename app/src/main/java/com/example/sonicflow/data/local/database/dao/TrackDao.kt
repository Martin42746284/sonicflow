package com.example.sonicflow.data.local.database.dao

import androidx.room.*
import com.example.sonicflow.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    // ✅ AJOUTER aussi cette méthode (optionnel mais recommandé)
    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: Long)

    @Query("UPDATE tracks SET waveformData = :waveformData WHERE id = :trackId")
    suspend fun updateWaveformData(trackId: Long, waveformData: String)

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY title ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>

    // ✅ AJOUTER cette méthode
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTracksCount(): Int

    // ✅ AJOUTER ces requêtes de tri
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getTracksSortedByTitle(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY artist ASC, title ASC")
    fun getTracksSortedByArtist(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY duration DESC")
    fun getTracksSortedByDuration(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getTracksSortedByDateAdded(): Flow<List<TrackEntity>>

    // ✅ Requêtes de tri - 8 variations
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getTracksSortedByTitleAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title DESC")
    fun getTracksSortedByTitleDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY artist ASC, title ASC")
    fun getTracksSortedByArtistAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY artist DESC, title ASC")
    fun getTracksSortedByArtistDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY duration ASC")
    fun getTracksSortedByDurationAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY duration DESC")
    fun getTracksSortedByDurationDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded ASC")
    fun getTracksSortedByDateAddedAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getTracksSortedByDateAddedDesc(): Flow<List<TrackEntity>>
}
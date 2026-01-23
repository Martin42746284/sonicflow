package com.example.sonicflow.data.local.database.dao

import androidx.room.*
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun delete(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun deleteAllTracksFromPlaylist(playlistId: Long)

    /**
     * Récupère toutes les pistes d'une playlist
     * CORRIGÉ : Annotation ajoutée pour éviter le warning
     */
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId
        WHERE playlist_track_cross_ref.playlistId = :playlistId
        ORDER BY playlist_track_cross_ref.addedAt DESC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    fun getCrossRefsForPlaylist(playlistId: Long): Flow<List<PlaylistTrackCrossRef>>

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM playlist_track_cross_ref 
            WHERE playlistId = :playlistId AND trackId = :trackId
        )
    """)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
}
package com.example.sonicflow.data.local.database.dao

import androidx.room.*
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun removeTrackFromPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylistById(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun removeAllTracksFromPlaylist(playlistId: Long)

    @Query("SELECT * FROM tracks INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId WHERE playlist_track_cross_ref.playlistId = :playlistId ORDER BY playlist_track_cross_ref.addedAt DESC")
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    @Query("SELECT playlistId FROM playlist_track_cross_ref WHERE trackId = :trackId")
    suspend fun getPlaylistIdsForTrack(trackId: Long): List<Long>
}

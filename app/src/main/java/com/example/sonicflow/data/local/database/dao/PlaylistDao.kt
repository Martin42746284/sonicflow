package com.example.sonicflow.data.local.database.dao

import androidx.room.*
import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.PlaylistWithTracks
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks>

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deletePlaylistTrackCrossRef(playlistId: Long, trackId: Long)

    @Query("SELECT * FROM playlists WHERE name LIKE :query ORDER BY name ASC")
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>

    // ✅ AJOUTER cette méthode
    @Query("SELECT EXISTS(SELECT 1 FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int
}
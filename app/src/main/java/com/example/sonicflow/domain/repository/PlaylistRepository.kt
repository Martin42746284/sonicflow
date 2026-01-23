package com.example.sonicflow.domain.repository

import com.example.sonicflow.data.local.entities.PlaylistWithTracks
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    fun getAllPlaylists(): Flow<List<Playlist>>

    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks>

    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    fun getPlaylistById(playlistId: Long): Flow<Playlist?>

    suspend fun createPlaylist(name: String, description: String?): Long

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun deletePlaylist(playlistId: Long)

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>

    fun searchPlaylists(query: String): Flow<List<Playlist>>

    // ✅ AJOUTER cette méthode
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    suspend fun getTrackCountForPlaylist(playlistId: Long): Int
}
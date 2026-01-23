package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.database.dao.PlaylistDao
import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.PlaylistWithTracks
import com.example.sonicflow.data.mapper.toModel
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks> {
        return playlistDao.getPlaylistWithTracks(playlistId)
    }

    override fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>> {
        return playlistDao.getAllPlaylistsWithTracks()
    }

    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistById(playlistId).map { it?.toModel() }
    }

    override suspend fun createPlaylist(name: String, description: String?): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            createdAt = playlist.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            addedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylistTrackCrossRef(crossRef)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deletePlaylistTrackCrossRef(playlistId, trackId)
    }

    override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getPlaylistWithTracks(playlistId).map { playlistWithTracks ->
            playlistWithTracks.tracks.map { it.toModel() }
        }
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylists("%$query%").map { entities ->
            entities.map { it.toModel() }
        }
    }

    // ✅ AJOUTER cette méthode
    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistDao.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun getTrackCountForPlaylist(playlistId: Long): Int {
        return playlistDao.getTrackCountForPlaylist(playlistId)
    }
}
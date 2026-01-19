package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.database.dao.PlaylistDao
import com.example.sonicflow.data.local.database.dao.PlaylistTrackCrossRefDao
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.mapper.*
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracksModel
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistTrackCrossRefDao: PlaylistTrackCrossRefDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { it.toDomainList() }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistById(playlistId)?.toDomain()
    }

    override suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracksModel? {
        return playlistDao.getPlaylistWithTracks(playlistId)?.toDomain()
    }

    override fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracksModel>> {
        return playlistDao.getAllPlaylistsWithTracks()
            .map { it.toDomainPlaylistWithTracksList() }
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(playlist.toEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }

    override suspend fun deletePlaylistById(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            addedAt = System.currentTimeMillis()
        )
        playlistTrackCrossRefDao.addTrackToPlaylist(crossRef)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTrackCrossRefDao.removeTrackFromPlaylistById(playlistId, trackId)
    }

    override suspend fun removeAllTracksFromPlaylist(playlistId: Long) {
        playlistTrackCrossRefDao.removeAllTracksFromPlaylist(playlistId)
    }

    override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistTrackCrossRefDao.getTracksForPlaylist(playlistId)
            .map { it.toDomainList() }
    }

    override suspend fun getTrackCountForPlaylist(playlistId: Long): Int {
        return playlistTrackCrossRefDao.getTrackCountForPlaylist(playlistId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistTrackCrossRefDao.isTrackInPlaylist(playlistId, trackId)
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylists(query).map { it.toDomainList() }
    }

    override suspend fun getPlaylistsCount(): Int {
        return playlistDao.getPlaylistsCount()
    }
}

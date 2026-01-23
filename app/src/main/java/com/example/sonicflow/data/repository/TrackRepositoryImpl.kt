package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.database.dao.TrackDao  // ✅ Correction de l'import
import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.data.mapper.toEntity
import com.example.sonicflow.data.mapper.toModel
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getTrackById(id: Long): Track? {
        return trackDao.getTrackById(id)?.toModel()
    }

    override suspend fun updateWaveformData(trackId: Long, waveformData: String) {
        trackDao.updateWaveformData(trackId, waveformData)
    }

    override suspend fun syncTracksFromDevice() {
        // TODO: Implémenter la synchronisation depuis le device
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getTracksByAlbum(album: String): Flow<List<Track>> {
        return trackDao.getTracksByAlbum(album).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getTracksByArtist(artist: String): Flow<List<Track>> {
        return trackDao.getTracksByArtist(artist).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track.toEntity())
    }

    override suspend fun deleteTrackById(trackId: Long) {
        trackDao.deleteTrackById(trackId)
    }

    override suspend fun getTracksCount(): Int {
        return trackDao.getTracksCount()
    }

    override fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> trackDao.getTracksSortedByTitleAsc()
            SortOrder.TITLE_DESC -> trackDao.getTracksSortedByTitleDesc()
            SortOrder.ARTIST_ASC -> trackDao.getTracksSortedByArtistAsc()
            SortOrder.ARTIST_DESC -> trackDao.getTracksSortedByArtistDesc()
            SortOrder.DURATION_ASC -> trackDao.getTracksSortedByDurationAsc()
            SortOrder.DURATION_DESC -> trackDao.getTracksSortedByDurationDesc()
            SortOrder.DATE_ADDED_ASC -> trackDao.getTracksSortedByDateAddedAsc()
            SortOrder.DATE_ADDED_DESC -> trackDao.getTracksSortedByDateAddedDesc()
        }.map { entities ->
            entities.map { it.toModel() }
        }
    }

    // ✅ AJOUTER cette méthode
    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track.toEntity())
    }
}
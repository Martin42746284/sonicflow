package com.example.sonicflow.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.example.sonicflow.data.local.database.dao.TrackDao
import com.example.sonicflow.data.mapper.toDomain
import com.example.sonicflow.data.mapper.toDomainList
import com.example.sonicflow.data.mapper.toEntity
import com.example.sonicflow.data.local.entities.TrackEntity
import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val contentResolver: ContentResolver
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { it.toDomainList() }
    }

    override suspend fun getTrackById(trackId: Long): Track? {
        return trackDao.getTrackById(trackId)?.toDomain()
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { it.toDomainList() }
    }

    override fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>> {
        return when (sortOrder) {
            SortOrder.TITLE -> trackDao.getTracksSortedByTitle()
            SortOrder.ARTIST -> trackDao.getTracksSortedByArtist()
            SortOrder.DURATION -> trackDao.getTracksSortedByDuration()
            SortOrder.DATE_ADDED -> trackDao.getTracksSortedByDateAdded()
        }.map { it.toDomainList() }
    }

    override suspend fun insertTrack(track: Track): Long {
        return trackDao.insertTrack(track.toEntity())
    }

    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track.toEntity())
    }

    override suspend fun syncTracksFromMediaStore() {
        val tracks = mutableListOf<TrackEntity>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000 // Convert to milliseconds
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                // Get album art URI
                val albumArtUri = ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()

                tracks.add(
                    TrackEntity(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        albumArtUri = albumArtUri,
                        dateAdded = dateAdded,
                        size = size,
                        mimeType = mimeType,
                        waveformData = null
                    )
                )
            }
        }

        // Insert all tracks into database
        trackDao.insertTracks(tracks)
    }

    override suspend fun updateWaveformData(trackId: Long, waveformData: String) {
        val track = trackDao.getTrackById(trackId)
        track?.let {
            val updatedTrack = it.copy(waveformData = waveformData)
            trackDao.updateTrack(updatedTrack)
        }
    }

    override suspend fun getTracksCount(): Int {
        return trackDao.getTracksCount()
    }
}

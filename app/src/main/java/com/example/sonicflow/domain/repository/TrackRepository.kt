package com.example.sonicflow.domain.repository

import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getAllTracks(): Flow<List<Track>>

    suspend fun getTrackById(id: Long): Track?

    suspend fun updateWaveformData(trackId: Long, waveformData: String)

    suspend fun syncTracksFromDevice()

    fun searchTracks(query: String): Flow<List<Track>>

    fun getTracksByAlbum(album: String): Flow<List<Track>>

    fun getTracksByArtist(artist: String): Flow<List<Track>>

    // ✅ AJOUTER cette méthode
    suspend fun deleteTrack(track: Track)

    // ✅ OPTIONNEL : Ajouter aussi deleteTrackById
    suspend fun deleteTrackById(trackId: Long)

    suspend fun getTracksCount(): Int

    // ✅ AJOUTER cette méthode
    fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>>

    suspend fun updateTrack(track: Track)
}
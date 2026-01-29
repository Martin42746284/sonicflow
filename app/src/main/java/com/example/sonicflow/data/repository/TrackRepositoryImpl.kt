package com.example.sonicflow.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.sonicflow.data.local.database.dao.TrackDao
import com.example.sonicflow.data.local.entities.TrackEntity
import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.data.mapper.toEntity
import com.example.sonicflow.data.mapper.toModel
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) : TrackRepository {

    companion object {
        private const val TAG = "TrackRepository"
    }

    // ==================== Récupération ====================

    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toModel() } // ✅ Utiliser le mapper
        }
    }

    override suspend fun getTrackById(id: Long): Track? {
        return trackDao.getTrackByIdSuspend(id)?.toModel() // ✅ Utiliser le mapper
    }

    override fun getTrackByIdFlow(id: Long): Flow<Track?> {
        return trackDao.getTrackById(id).map { it?.toModel() } // ✅ Utiliser le mapper
    }

    override suspend fun getTracksByIds(ids: List<Long>): List<Track> {
        return trackDao.getTracksByIds(ids).map { it.toModel() } // ✅ Utiliser le mapper
    }

    override suspend fun getTracksCount(): Int {
        return trackDao.getTracksCount()
    }

    // ==================== Insertion / Mise à jour ====================

    override suspend fun insertTrack(track: Track) {
        trackDao.insertTrack(track.toEntity()) // ✅ Utiliser le mapper
    }

    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track.toEntity()) // ✅ Utiliser le mapper
    }

    override suspend fun updateWaveformData(trackId: Long, waveformData: String) {
        trackDao.updateWaveformData(trackId, waveformData)
    }

    override suspend fun incrementPlayCount(trackId: Long) {
        trackDao.incrementPlayCount(trackId)
    }

    override suspend fun updateLastPlayedDate(trackId: Long, timestamp: Long) {
        trackDao.updateLastPlayedDate(trackId, timestamp)
    }

    // ==================== Suppression ====================

    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track.toEntity()) // ✅ Utiliser le mapper
    }

    override suspend fun deleteTrackById(trackId: Long) {
        trackDao.deleteTrackById(trackId)
    }

    override suspend fun deleteAllTracks() {
        trackDao.deleteAll()
    }

    // ==================== Synchronisation MediaStore ====================

    override suspend fun syncTracks() {
        try {
            Log.d(TAG, "🎵 Début du scan des fichiers audio...")

            val tracks = scanAudioFiles()

            Log.d(TAG, "✅ ${tracks.size} fichiers audio trouvés")

            if (tracks.isNotEmpty()) {
                // Supprimer les anciennes entrées
                trackDao.deleteAll()

                // Insérer les nouvelles
                trackDao.insertTracks(tracks.map { it.toEntity() }) // ✅ Utiliser le mapper

                Log.d(TAG, "✅ Base de données mise à jour avec ${tracks.size} pistes")
            } else {
                Log.w(TAG, "⚠️ Aucun fichier audio trouvé sur l'appareil")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du scan: ${e.message}", e)
            throw e
        }
    }

    private fun scanAudioFiles(): List<Track> {
        val tracks = mutableListOf<Track>()
        val contentResolver: ContentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                Log.d(TAG, "📂 Nombre de fichiers trouvés: ${cursor.count}")

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "Unknown Title"
                        val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                        val album = cursor.getString(albumColumn) ?: "Unknown Album"
                        val duration = cursor.getLong(durationColumn)
                        val path = cursor.getString(dataColumn) ?: "" // ✅ 'path' au lieu de 'data'
                        val albumId = cursor.getLong(albumIdColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn) * 1000

                        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")

                        val track = Track(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            albumArtUri = albumArtUri.toString(),
                            path = path, // ✅ Utiliser 'path'
                            dateAdded = dateAdded,
                            waveformData = null
                        )

                        tracks.add(track)
                        Log.d(TAG, "🎵 Ajouté: $title - $artist (${duration}ms)")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur lors de la lecture d'une piste: ${e.message}")
                        continue
                    }
                }
            } ?: run {
                Log.e(TAG, "❌ Le cursor est null")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException: Permission refusée", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la lecture du MediaStore", e)
            throw e
        }

        return tracks
    }

    // ==================== Recherche ====================

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracksFlow("%$query%").map { entities ->
            entities.map { it.toModel() } // ✅ Utiliser le mapper
        }
    }

    override suspend fun searchTracksAsync(query: String): List<Track> {
        return trackDao.searchTracks("%$query%").map { it.toModel() } // ✅ Utiliser le mapper
    }

    // ==================== Filtres ====================

    override fun getTracksByAlbum(album: String): Flow<List<Track>> {
        return trackDao.getTracksByAlbum(album).map { entities ->
            entities.map { it.toModel() } // ✅ Utiliser le mapper
        }
    }

    override fun getTracksByArtist(artist: String): Flow<List<Track>> {
        return trackDao.getTracksByArtist(artist).map { entities ->
            entities.map { it.toModel() } // ✅ Utiliser le mapper
        }
    }

    override fun getAllAlbums(): Flow<List<String>> {
        return trackDao.getAllAlbums()
    }

    override fun getAllArtists(): Flow<List<String>> {
        return trackDao.getAllArtists()
    }

    // ==================== Tri ====================

    override fun getTracksSorted(sortOrder: SortOrder): Flow<List<Track>> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> trackDao.getTracksSortedByTitle(ascending = true)
            SortOrder.TITLE_DESC -> trackDao.getTracksSortedByTitle(ascending = false)
            SortOrder.ARTIST_ASC -> trackDao.getTracksSortedByArtist(ascending = true)
            SortOrder.ARTIST_DESC -> trackDao.getTracksSortedByArtist(ascending = false)
            SortOrder.DURATION_ASC -> trackDao.getTracksSortedByDuration(ascending = true)
            SortOrder.DURATION_DESC -> trackDao.getTracksSortedByDuration(ascending = false)
            SortOrder.DATE_ADDED_ASC -> trackDao.getTracksSortedByDateAdded(ascending = true)
            SortOrder.DATE_ADDED_DESC -> trackDao.getTracksSortedByDateAdded(ascending = false)
        }.map { entities ->
            entities.map { it.toModel() } // ✅ Utiliser le mapper
        }
    }

    // ==================== Utilitaires ====================

    override suspend fun trackExistsByPath(path: String): Boolean {
        return trackDao.trackExistsByPath(path)
    }
}
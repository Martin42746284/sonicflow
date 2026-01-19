package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracksModel
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    /**
     * Récupère toutes les playlists
     */
    fun getAllPlaylists(): Flow<List<Playlist>>

    /**
     * Récupère une playlist par son ID
     */
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    /**
     * Récupère une playlist avec toutes ses pistes
     */
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracksModel?

    /**
     * Récupère toutes les playlists avec leurs pistes
     */
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracksModel>>

    /**
     * Crée une nouvelle playlist
     * @return l'ID de la playlist créée
     */
    suspend fun createPlaylist(playlist: Playlist): Long

    /**
     * Met à jour une playlist existante
     */
    suspend fun updatePlaylist(playlist: Playlist)

    /**
     * Supprime une playlist
     */
    suspend fun deletePlaylist(playlist: Playlist)

    /**
     * Supprime une playlist par son ID
     */
    suspend fun deletePlaylistById(playlistId: Long)

    /**
     * Ajoute une piste à une playlist
     */
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)

    /**
     * Retire une piste d'une playlist
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    /**
     * Retire toutes les pistes d'une playlist
     */
    suspend fun removeAllTracksFromPlaylist(playlistId: Long)

    /**
     * Récupère toutes les pistes d'une playlist
     */
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>

    /**
     * Récupère le nombre de pistes dans une playlist
     */
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int

    /**
     * Vérifie si une piste est dans une playlist
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    /**
     * Recherche des playlists par nom
     */
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    /**
     * Récupère le nombre total de playlists
     */
    suspend fun getPlaylistsCount(): Int
}

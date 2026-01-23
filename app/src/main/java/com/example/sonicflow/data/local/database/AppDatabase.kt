package com.example.sonicflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sonicflow.data.local.database.dao.PlaylistDao
import com.example.sonicflow.data.local.database.dao.TrackDao
import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.TrackEntity

/**
 * Base de données Room pour SonicFlow
 * Contient les tables pour les tracks et les playlists
 */
@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO pour les opérations sur les tracks
     */
    abstract fun trackDao(): TrackDao

    /**
     * DAO pour les opérations sur les playlists
     */
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "sonicflow_database"
    }
}
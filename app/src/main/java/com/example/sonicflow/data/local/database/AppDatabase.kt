package com.example.sonicflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.sonicflow.data.local.database.dao.PlaylistDao
import com.example.sonicflow.data.local.database.dao.PlaylistTrackCrossRefDao
import com.example.sonicflow.data.local.database.dao.TrackDao
import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.TrackEntity
import java.util.Date

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

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackCrossRefDao(): PlaylistTrackCrossRefDao

    companion object {
        const val DATABASE_NAME = "sonicflow_database"
    }
}

/**
 * TypeConverters pour les types personnalisés
 * Permet à Room de stocker des types complexes
 */
class Converters {

    /**
     * Convertit Date en Long pour stockage
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convertit Long en Date pour récupération
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Convertit une liste de Long en String pour stockage
     */
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return value?.joinToString(",")
    }

    /**
     * Convertit String en liste de Long pour récupération
     */
    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return value?.split(",")?.mapNotNull { it.toLongOrNull() }
    }

    /**
     * Convertit une liste de String en String pour stockage
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString("||")
    }

    /**
     * Convertit String en liste de String pour récupération
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("||")?.filter { it.isNotEmpty() }
    }
}
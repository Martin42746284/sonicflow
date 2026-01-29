package com.example.sonicflow.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"])
    ]
)
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val albumArtUri: String?,
    val data: String,
    val dateAdded: Long,
    val waveformData: String? = null,
    val playCount: Int = 0,
    val lastPlayedDate: Long? = null,
    val isFavorite: Boolean = false
)

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long,
    val path: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val waveformData: String? = null
)
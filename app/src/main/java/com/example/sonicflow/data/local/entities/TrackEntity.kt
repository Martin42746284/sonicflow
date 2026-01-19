package com.example.sonicflow.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long, // en millisecondes
    val path: String, // chemin du fichier audio
    val albumArtUri: String?,
    val dateAdded: Long, // timestamp
    val size: Long, // taille du fichier en bytes
    val mimeType: String?,
    val waveformData: String? = null // JSON string des amplitudes pour la waveform
)

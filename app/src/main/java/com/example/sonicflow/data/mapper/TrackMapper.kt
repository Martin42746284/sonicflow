package com.example.sonicflow.data.mapper

import com.example.sonicflow.data.local.entities.TrackEntity
import com.example.sonicflow.domain.model.Track

/**
 * Convertit une TrackEntity en Track (modèle du domaine)
 */
fun TrackEntity.toModel(): Track {
    return Track(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album ?: "Unknown Album",
        duration = this.duration,
        path = this.data,
        albumArtUri = this.albumArtUri,
        dateAdded = this.dateAdded,
        waveformData = this.waveformData
    )
}

/**
 * Convertit un Track en TrackEntity
 */
fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration,
        data = this.path,
        albumArtUri = this.albumArtUri,
        dateAdded = this.dateAdded,
        waveformData = this.waveformData
    )
}

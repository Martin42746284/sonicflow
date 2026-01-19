package com.example.sonicflow.data.mapper

import com.example.sonicflow.data.local.entities.TrackEntity
import com.example.sonicflow.domain.model.Track

fun TrackEntity.toDomain(): Track {
    return Track(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration,
        path = this.path,
        albumArtUri = this.albumArtUri,
        dateAdded = this.dateAdded,
        size = this.size,
        mimeType = this.mimeType,
        waveformData = this.waveformData
    )
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration,
        path = this.path,
        albumArtUri = this.albumArtUri,
        dateAdded = this.dateAdded,
        size = this.size,
        mimeType = this.mimeType,
        waveformData = this.waveformData
    )
}

fun List<TrackEntity>.toDomainList(): List<Track> {
    return this.map { it.toDomain() }
}

fun List<Track>.toEntityList(): List<TrackEntity> {
    return this.map { it.toEntity() }
}

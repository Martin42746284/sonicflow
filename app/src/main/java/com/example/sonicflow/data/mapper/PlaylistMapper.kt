package com.example.sonicflow.data.mapper

import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistWithTracks
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracksModel

fun PlaylistEntity.toDomain(): Playlist {
    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        coverImagePath = this.coverImagePath
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        coverImagePath = this.coverImagePath
    )
}

fun PlaylistWithTracks.toDomain(): PlaylistWithTracksModel {
    return PlaylistWithTracksModel(
        playlist = this.playlist.toDomain(),
        tracks = this.tracks.toDomainList()
    )
}

fun List<PlaylistEntity>.toDomainList(): List<Playlist> {
    return this.map { it.toDomain() }
}

fun List<Playlist>.toEntityList(): List<PlaylistEntity> {
    return this.map { it.toEntity() }
}

fun List<PlaylistWithTracks>.toDomainPlaylistWithTracksList(): List<PlaylistWithTracksModel> {
    return this.map { it.toDomain() }
}

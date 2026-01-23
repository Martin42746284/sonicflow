package com.example.sonicflow.data.mapper

import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.domain.model.Playlist

/**
 * Convertit une PlaylistEntity en Playlist (modèle du domaine)
 */
fun PlaylistEntity.toModel(): Playlist {
    return Playlist(
        id = id,
        name = name,
        description = description,
        coverImagePath = coverImagePath,
        trackCount = 0, // Sera mis à jour via la relation
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convertit un Playlist en PlaylistEntity
 */
fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        description = description,
        coverImagePath = coverImagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
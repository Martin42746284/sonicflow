package com.example.sonicflow.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Classe de relation pour récupérer une playlist avec toutes ses pistes
 */
data class PlaylistWithTracks(
    @Embedded
    val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "trackId"
        )
    )
    val tracks: List<TrackEntity>
)
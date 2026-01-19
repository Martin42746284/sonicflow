package com.example.sonicflow.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TrackWithPlaylists(
    @Embedded
    val track: TrackEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = "trackId",
            entityColumn = "playlistId"
        )
    )
    val playlists: List<PlaylistEntity>
)
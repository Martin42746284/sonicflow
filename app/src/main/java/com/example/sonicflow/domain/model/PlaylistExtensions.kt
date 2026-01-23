package com.example.sonicflow.domain.model

/**
 * Met à jour une playlist avec de nouveaux paramètres
 */
fun Playlist.update(
    name: String? = null,
    description: String? = null,
    coverImagePath: String? = null
): Playlist {
    return this.copy(
        name = name ?: this.name,
        description = description ?: this.description,
        coverImagePath = coverImagePath ?: this.coverImagePath,
        updatedAt = System.currentTimeMillis()
    )
}
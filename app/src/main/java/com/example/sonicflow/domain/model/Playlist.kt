package com.example.sonicflow.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val coverImagePath: String? = null,
    val trackCount: Int = 0
) {
    companion object {
        // Factory method pour créer une nouvelle playlist
        fun create(
            name: String,
            description: String? = null
        ): Playlist {
            val currentTime = System.currentTimeMillis()
            return Playlist(
                name = name, // Room va auto-générer l'ID
                description = description,
                createdAt = currentTime,
                updatedAt = currentTime,
            )
        }
    }

    // Fonction pour mettre à jour la playlist
    fun update(
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

    // Formater la date de création
    fun getFormattedCreatedDate(): String {
        val date = java.util.Date(createdAt)
        val format = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }
}

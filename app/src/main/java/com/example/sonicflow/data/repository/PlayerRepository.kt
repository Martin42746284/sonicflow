package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.RepeatMode
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gérer l'état du lecteur audio
 */
interface PlayerRepository {

    /**
     * Jouer une piste
     */
    suspend fun playTrack(track: Track)

    /**
     * Mettre en pause la lecture
     */
    suspend fun pauseTrack()

    /**
     * Reprendre la lecture
     */
    suspend fun resumeTrack()

    /**
     * Passer à la piste suivante
     */
    suspend fun skipToNext()

    /**
     * Revenir à la piste précédente
     */
    suspend fun skipToPrevious()

    /**
     * Se déplacer à une position spécifique
     */
    suspend fun seekTo(position: Long)

    /**
     * Obtenir la piste actuellement en cours de lecture
     */
    fun getCurrentTrack(): Flow<Track?>

    /**
     * Obtenir l'état de lecture (en cours ou en pause)
     */
    fun isPlaying(): Flow<Boolean>

    /**
     * Obtenir la position actuelle
     */
    fun getCurrentPosition(): Flow<Long>

    /**
     * Obtenir la durée totale de la piste
     */
    fun getDuration(): Flow<Long>

    /**
     * Obtenir le mode repeat
     */
    fun getRepeatMode(): Flow<RepeatMode>

    /**
     * Définir le mode repeat
     */
    suspend fun setRepeatMode(mode: RepeatMode)

    /**
     * Basculer le mode repeat
     */
    suspend fun toggleRepeatMode()

    /**
     * Obtenir l'état shuffle
     */
    fun getShuffleEnabled(): Flow<Boolean>

    /**
     * Définir l'état shuffle
     */
    suspend fun setShuffleEnabled(enabled: Boolean)

    /**
     * Basculer le mode shuffle
     */
    suspend fun toggleShuffleMode()

    /**
     * Obtenir la file d'attente
     */
    fun getQueue(): Flow<List<Track>>

    /**
     * Définir la file d'attente
     */
    suspend fun setQueue(tracks: List<Track>)

    /**
     * Libérer les ressources du player
     */
    suspend fun release()
}

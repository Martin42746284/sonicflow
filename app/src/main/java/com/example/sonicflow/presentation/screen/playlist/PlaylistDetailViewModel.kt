package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.mapper.toModel
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran de détail de playlist
 * Semaine 3, Jours 18-19
 */
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getTracksForPlaylistUseCase: GetTracksForPlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailState())
    val state: StateFlow<PlaylistDetailState> = _state.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Charger les infos de la playlist via Flow
                getPlaylistsUseCase.getPlaylistWithTracks(playlistId)
                    .catch { exception ->
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load playlist"
                            )
                        }
                    }
                    .collect { playlistWithTracks ->
                        // Convertir les entités en modèles du domaine
                        val playlist = playlistWithTracks.playlist.toModel()
                        val tracks = playlistWithTracks.tracks.map { trackEntity ->
                            trackEntity.toModel()
                        }

                        val totalDuration = formatTotalDuration(
                            tracks.sumOf { track -> track.duration }
                        )

                        _state.update { currentState ->
                            currentState.copy(
                                playlist = playlist,
                                tracks = tracks,
                                totalDuration = totalDuration,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, description: String?) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                // Mettre à jour le nom
                updatePlaylistUseCase.updateName(playlistId, name)

                // Mettre à jour la description si fournie
                if (description != null) {
                    updatePlaylistUseCase.updateDescription(playlistId, description)
                }

                // Recharger la playlist
                loadPlaylist(playlistId)
            } catch (exception: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to update playlist"
                )}
            }
        }
    }

    fun removeTrack(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                // Supprimer la track de la playlist
                removeTrackFromPlaylistUseCase(playlistId, trackId)

                // Mise à jour optimiste : retirer immédiatement de l'UI
                _state.update { currentState ->
                    val updatedTracks = currentState.tracks.filter { it.id != trackId }
                    val updatedDuration = formatTotalDuration(
                        updatedTracks.sumOf { it.duration }
                    )

                    currentState.copy(
                        tracks = updatedTracks,
                        totalDuration = updatedDuration,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to remove track"
                )}

                // Recharger pour être sûr d'avoir les bonnes données
                loadPlaylist(playlistId)
            }
        }
    }

    /**
     * Jouer toute la playlist
     */
    fun playPlaylist() {
        val currentTracks = _state.value.tracks
        if (currentTracks.isNotEmpty()) {
            // TODO: Intégrer avec PlayerViewModel
            // playerViewModel.playTrack(currentTracks.first(), currentTracks)
        }
    }

    /**
     * Jouer la playlist en mode aléatoire
     */
    fun shufflePlaylist() {
        val currentTracks = _state.value.tracks
        if (currentTracks.isNotEmpty()) {
            val shuffledTracks = currentTracks.shuffled()
            // TODO: Intégrer avec PlayerViewModel
            // playerViewModel.playTrack(shuffledTracks.first(), shuffledTracks)
        }
    }

    /**
     * Jouer une piste spécifique dans le contexte de la playlist
     */
    fun playTrack(track: Track) {
        val currentTracks = _state.value.tracks
        // TODO: Intégrer avec PlayerViewModel
        // playerViewModel.playTrack(track, currentTracks)
    }

    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Formatte la durée totale en format lisible
     * Ex: "1h 23min" ou "45min"
     */
    private fun formatTotalDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}min"
            minutes > 0 -> "${minutes}min"
            else -> "< 1min"
        }
    }

    /**
     * Rafraîchir les données de la playlist
     */
    fun refresh() {
        val currentPlaylistId = _state.value.playlist?.id
        if (currentPlaylistId != null) {
            loadPlaylist(currentPlaylistId)
        }
    }
}

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = "0min",
    val isLoading: Boolean = false,
    val error: String? = null
)
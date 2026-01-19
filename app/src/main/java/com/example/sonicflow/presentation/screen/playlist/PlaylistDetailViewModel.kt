package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // Charger les infos de la playlist
                getPlaylistsUseCase.getPlaylistWithTracks(playlistId).fold(
                    onSuccess = { playlistWithTracks ->
                        val totalDuration = formatTotalDuration(
                            playlistWithTracks.tracks.sumOf { it.duration }
                        )

                        _state.update { it.copy(
                            playlist = playlistWithTracks.playlist,
                            tracks = playlistWithTracks.tracks,
                            totalDuration = totalDuration,
                            isLoading = false
                        )}
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load playlist"
                        )}
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )}
            }
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, description: String?) {
        viewModelScope.launch {
            updatePlaylistUseCase.updateName(playlistId, name)
            updatePlaylistUseCase.updateDescription(playlistId, description)

            // Recharger la playlist
            loadPlaylist(playlistId)
        }
    }

    fun removeTrack(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            removeTrackFromPlaylistUseCase(playlistId, trackId).fold(
                onSuccess = {
                    // Recharger la playlist
                    loadPlaylist(playlistId)
                },
                onFailure = { exception ->
                    _state.update { it.copy(
                        error = exception.message ?: "Failed to remove track"
                    )}
                }
            )
        }
    }

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
}

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

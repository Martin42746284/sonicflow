package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran des playlists
 * Semaine 3, Jours 18-19
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getPlaylistsUseCase.getPlaylistsWithTracks()
                .catch { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load playlists"
                    )}
                }
                .collect { playlistsWithTracks ->
                    val trackCounts = playlistsWithTracks.associate {
                        it.playlist.id to it.trackCount
                    }

                    _state.update { it.copy(
                        playlists = playlistsWithTracks.map { it.playlist },
                        playlistTrackCounts = trackCounts,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }

    fun createPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            createPlaylistUseCase(name, description).fold(
                onSuccess = { playlistId ->
                    _state.update { it.copy(
                        isLoading = false,
                        successMessage = "Playlist created successfully"
                    )}
                    // Les playlists se mettront à jour automatiquement via le Flow
                },
                onFailure = { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create playlist"
                    )}
                }
            )
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            deletePlaylistUseCase(playlistId).fold(
                onSuccess = {
                    _state.update { it.copy(
                        successMessage = "Playlist deleted"
                    )}
                },
                onFailure = { exception ->
                    _state.update { it.copy(
                        error = exception.message ?: "Failed to delete playlist"
                    )}
                }
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}

data class PlaylistScreenState(
    val playlists: List<Playlist> = emptyList(),
    val playlistTrackCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

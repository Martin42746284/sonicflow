package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.mapper.toModel
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
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoading = true, error = null)
            }

            try {
                // Récupérer toutes les playlists avec leurs tracks
                getPlaylistsUseCase()
                    .catch { exception ->
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load playlists"
                            )
                        }
                    }
                    .collect { playlists ->
                        _state.update { currentState ->
                            currentState.copy(
                                playlists = playlists,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (exception: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(isLoading = true)
                }

                createPlaylistUseCase(name, description)

                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        successMessage = "Playlist created successfully"
                    )
                }
            } catch (exception: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create playlist"
                    )
                }
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlistId)

                _state.update { currentState ->
                    currentState.copy(
                        successMessage = "Playlist deleted"
                    )
                }
            } catch (exception: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        error = exception.message ?: "Failed to delete playlist"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { currentState ->
            currentState.copy(error = null, successMessage = null)
        }
    }
}

data class PlaylistScreenState(
    val playlists: List<Playlist> = emptyList(),
    val playlistTrackCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
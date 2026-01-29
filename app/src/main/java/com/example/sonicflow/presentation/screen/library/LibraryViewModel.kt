package com.example.sonicflow.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.local.preferences.SortOrder
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.usecase.track.GetTracksUseCase
import com.example.sonicflow.domain.usecase.track.SearchTracksUseCase
import com.example.sonicflow.domain.usecase.track.SortTracksUseCase
import com.example.sonicflow.domain.usecase.track.SyncTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran Library
 * Semaine 1, Jours 6-7
 * Avec scan automatique au démarrage
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val searchTracksUseCase: SearchTracksUseCase,
    private val sortTracksUseCase: SortTracksUseCase,
    private val syncTracksUseCase: SyncTracksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)

    init {
        observeTracks()
        // ✅ Le scan automatique sera déclenché depuis LibraryScreen après la permission
    }

    private fun observeTracks() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _sortOrder
            ) { query, sortOrder ->
                Pair(query, sortOrder)
            }.flatMapLatest { (query, sortOrder) ->
                if (query.isBlank()) {
                    sortTracksUseCase(sortOrder)
                } else {
                    searchTracksUseCase(query).map { tracks ->
                        // Trier les résultats de recherche aussi
                        sortTracksList(tracks, sortOrder)
                    }
                }
            }.catch { exception ->
                _state.update { it.copy(
                    error = exception.message ?: "Unknown error occurred",
                    isLoading = false
                )}
            }.collect { tracks ->
                _state.update { it.copy(
                    tracks = tracks,
                    isLoading = false,
                    error = null
                )}
            }
        }
    }

    /**
     * ✅ Méthode publique pour scanner les pistes
     * Appelée automatiquement depuis LibraryScreen après l'obtention de la permission
     */
    fun syncTracks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                syncTracksUseCase()
                _state.update { it.copy(isLoading = false) }
            } catch (exception: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to sync tracks"
                )}
            }
        }
    }

    /**
     * ✅ Vérifier si un scan est nécessaire (base de données vide)
     * Utilisé pour décider si on doit scanner automatiquement
     */
    fun isSyncNeeded(): Boolean {
        return state.value.tracks.isEmpty()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
        _state.update { it.copy(sortOrder = sortOrder) }
    }

    /**
     * Helper pour trier une liste de tracks
     */
    private fun sortTracksList(tracks: List<Track>, sortOrder: SortOrder): List<Track> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> tracks.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> tracks.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> tracks.sortedBy { it.artist.lowercase() }
            SortOrder.ARTIST_DESC -> tracks.sortedByDescending { it.artist.lowercase() }
            SortOrder.DURATION_ASC -> tracks.sortedBy { it.duration }
            SortOrder.DURATION_DESC -> tracks.sortedByDescending { it.duration }
            SortOrder.DATE_ADDED_ASC -> tracks.sortedBy { it.dateAdded }
            SortOrder.DATE_ADDED_DESC -> tracks.sortedByDescending { it.dateAdded }
        }
    }

    /**
     * ✅ Méthode pour forcer un refresh manuel (bouton refresh)
     */
    fun refreshTracks() {
        syncTracks()
    }

    /**
     * ✅ Réinitialiser l'erreur
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class LibraryState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.TITLE_ASC
)
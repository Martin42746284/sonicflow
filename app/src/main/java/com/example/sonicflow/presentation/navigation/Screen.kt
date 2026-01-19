package com.example.sonicflow.presentation.navigation

/**
 * Sealed class représentant tous les écrans de l'application
 * Permet une navigation type-safe
 */
sealed class Screen(val route: String) {

    // Authentication
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")

    // Main
    data object Library : Screen("library")
    data object Player : Screen("player")
    data object Playlists : Screen("playlists")

    // Detail screens avec arguments
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: Long): String = "playlist_detail/$playlistId"
    }

    // Settings (pour extension future)
    data object Settings : Screen("settings")

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("/")) {
                SignIn.route -> SignIn
                SignUp.route -> SignUp
                Library.route -> Library
                Player.route -> Player
                Playlists.route -> Playlists
                "playlist_detail" -> PlaylistDetail
                "settings" -> Settings
                else -> null
            }
        }
    }
}
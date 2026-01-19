package com.example.sonicflow.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Extensions pour faciliter la navigation
 */

/**
 * Navigate et clear backstack jusqu'à une route spécifique
 */
fun NavController.navigateAndClearBackStack(route: String, popUpToRoute: String) {
    navigate(route) {
        popUpTo(popUpToRoute) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Navigate avec single top (évite les doublons dans la pile)
 */
fun NavController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Navigate vers le player avec la piste en argument (si nécessaire)
 */
fun NavController.navigateToPlayer(clearBackStack: Boolean = false) {
    navigate(Screen.Player.route) {
        if (clearBackStack) {
            popUpTo(Screen.Library.route)
        }
        launchSingleTop = true
    }
}

/**
 * Navigate vers une playlist spécifique
 */
fun NavController.navigateToPlaylistDetail(playlistId: Long) {
    navigate(Screen.PlaylistDetail.createRoute(playlistId))
}

/**
 * Revenir à la bibliothèque et clear tout au-dessus
 */
fun NavController.navigateBackToLibrary() {
    navigate(Screen.Library.route) {
        popUpTo(Screen.Library.route) { inclusive = true }
        launchSingleTop = true
    }
}
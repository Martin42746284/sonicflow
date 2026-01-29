package com.example.sonicflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sonicflow.presentation.screen.auth.SignInScreen
import com.example.sonicflow.presentation.screen.auth.SignUpScreen
import com.example.sonicflow.presentation.screen.library.LibraryScreen
import com.example.sonicflow.presentation.screen.player.PlayerScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistScreen

/**
 * Graphe de navigation principal avec type-safety
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Library.route // ✅ Démarrer directement sur Library (enlever auth pour dev)
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ===== Authentication =====

        composable(route = Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== Main Screens =====

        composable(route = Screen.Library.route) {
            LibraryScreen(
                // ✅ Plus besoin de onTrackClick, géré directement dans LibraryScreen
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        composable(route = Screen.Player.route) {
            PlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Playlists.route) {
            PlaylistScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable

            PlaylistDetailScreen(
                playlistId = playlistId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTrackClick = { track ->
                    navController.navigate(Screen.Player.route)
                }
            )
        }
    }
}
package com.example.sonicflow.presentation.util

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extensions utilitaires pour SonicFlow
 */

// ===== Context Extensions =====

/**
 * Affiche un Toast court
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Affiche un Toast long
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ===== Flow Extensions =====

/**
 * Collecte un StateFlow comme State dans Compose avec un état initial
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(): State<T> {
    return collectAsState()
}

// ===== Time/Duration Extensions =====

/**
 * Formate une durée en millisecondes vers MM:SS
 */
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Formate une durée en millisecondes vers HH:MM:SS
 */
fun Long.formatLongDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Formate un timestamp en date lisible
 */
fun Long.formatDate(pattern: String = "dd MMM yyyy"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(date)
}

/**
 * Formate un timestamp en date et heure
 */
fun Long.formatDateTime(pattern: String = "dd MMM yyyy HH:mm"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(date)
}

// ===== Size Extensions =====

/**
 * Formate une taille en bytes vers une chaîne lisible
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${this / 1024} KB"
        this < 1024 * 1024 * 1024 -> String.format("%.2f MB", this / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", this / (1024.0 * 1024.0 * 1024.0))
    }
}

// ===== String Extensions =====

/**
 * Capitalise la première lettre
 */
fun String.capitalizeFirst(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

/**
 * Tronque une chaîne si elle dépasse une longueur maximale
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Vérifie si une chaîne est un email valide
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return this.matches(emailRegex)
}

// ===== List Extensions =====

/**
 * Récupère un élément de manière sûre avec un index
 */
fun <T> List<T>.getSafe(index: Int): T? {
    return if (index in indices) this[index] else null
}

/**
 * Divise une liste en chunks de taille spécifique
 */
fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return this.windowed(size, size, partialWindows = true)
}

// ===== Number Extensions =====

/**
 * Limite un nombre dans un intervalle
 */
fun Int.coerceInRange(min: Int, max: Int): Int {
    return this.coerceIn(min, max)
}

/**
 * Limite un Float dans un intervalle
 */
fun Float.coerceInRange(min: Float, max: Float): Float {
    return this.coerceIn(min, max)
}

/**
 * Convertit un Float en pourcentage
 */
fun Float.toPercentage(): String {
    return "${(this * 100).toInt()}%"
}

// ===== Collection Extensions =====

/**
 * Trouve le premier élément qui satisfait le prédicat ou retourne une valeur par défaut
 */
inline fun <T> Iterable<T>.firstOrDefault(default: T, predicate: (T) -> Boolean): T {
    return this.firstOrNull(predicate) ?: default
}

/**
 * Vérifie si une collection est vide ou nulle
 */
fun <T> Collection<T>?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * Retourne une liste non-null ou une liste vide
 */
fun <T> List<T>?.orEmpty(): List<T> {
    return this ?: emptyList()
}

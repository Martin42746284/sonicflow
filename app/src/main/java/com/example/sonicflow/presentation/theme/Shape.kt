package com.example.sonicflow.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formes personnalisées pour SonicFlow
 * Utilise des coins arrondis pour une apparence moderne
 */
val Shapes = Shapes(
    // Extra small (4dp) - chips, small components
    extraSmall = RoundedCornerShape(4.dp),

    // Small (8dp) - buttons, cards
    small = RoundedCornerShape(8.dp),

    // Medium (12dp) - dialogs, bottom sheets
    medium = RoundedCornerShape(12.dp),

    // Large (16dp) - large cards, album art
    large = RoundedCornerShape(16.dp),

    // Extra large (24dp) - special components
    extraLarge = RoundedCornerShape(24.dp)
)

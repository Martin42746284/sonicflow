package com.example.sonicflow.presentation.screen.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Composant de visualisation de waveform dynamique
 * Semaine 4, Jours 25-26 : UI Waveform avec Compose Canvas
 */
@Composable
fun WaveformView(
    amplitudes: List<Float>,
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveformColor: Color = MaterialTheme.colorScheme.primary,
    progressColor: Color = MaterialTheme.colorScheme.tertiary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
) {
    var canvasWidth by remember { mutableStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (canvasWidth > 0) {
                        val seekPosition = (offset.x / canvasWidth).coerceIn(0f, 1f)
                        onSeek(seekPosition)
                    }
                }
            }
    ) {
        canvasWidth = size.width

        if (amplitudes.isEmpty()) {
            // Afficher une ligne plate si pas de données
            drawLine(
                color = waveformColor.copy(alpha = 0.3f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val barWidth = size.width / amplitudes.size
        val centerY = size.height / 2
        val progressX = progress * size.width

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2
            val barHeight = amplitude * size.height * 0.8f // 80% de la hauteur max

            // Déterminer la couleur en fonction de la progression
            val color = if (x <= progressX) progressColor else waveformColor

            // Dessiner la barre de waveform
            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = (barWidth * 0.8f).coerceAtLeast(2f),
                cap = StrokeCap.Round
            )
        }

        // Dessiner un indicateur de progression vertical
        drawLine(
            color = progressColor,
            start = Offset(progressX, 0f),
            end = Offset(progressX, size.height),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Version simplifiée de la waveform (barres verticales simples)
 */
@Composable
fun SimpleWaveformView(
    amplitudes: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        if (amplitudes.isEmpty()) return@Canvas

        val barWidth = size.width / amplitudes.size
        val progressX = progress * size.width

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2
            val barHeight = amplitude * size.height

            val color = if (x <= progressX) activeColor else inactiveColor

            drawLine(
                color = color,
                start = Offset(x, size.height - barHeight),
                end = Offset(x, size.height),
                strokeWidth = (barWidth * 0.7f).coerceAtLeast(1.5f),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Waveform circulaire (style radial)
 */
@Composable
fun CircularWaveformView(
    amplitudes: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier,
    waveformColor: Color = MaterialTheme.colorScheme.primary,
    progressColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(
        modifier = modifier
            .size(200.dp)
    ) {
        if (amplitudes.isEmpty()) return@Canvas

        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = size.minDimension * 0.3f
        val maxRadius = size.minDimension * 0.45f

        val angleStep = 360f / amplitudes.size
        val progressAngle = progress * 360f

        amplitudes.forEachIndexed { index, amplitude ->
            val angle = index * angleStep - 90f // Start from top
            val isPlayed = angle + 90f <= progressAngle

            val radius = baseRadius + (amplitude * (maxRadius - baseRadius))

            val radians = Math.toRadians(angle.toDouble())
            val startX = centerX + (baseRadius * Math.cos(radians)).toFloat()
            val startY = centerY + (baseRadius * Math.sin(radians)).toFloat()
            val endX = centerX + (radius * Math.cos(radians)).toFloat()
            val endY = centerY + (radius * Math.sin(radians)).toFloat()

            drawLine(
                color = if (isPlayed) progressColor else waveformColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Mini waveform pour les vignettes de pistes
 */
@Composable
fun MiniWaveformView(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        if (amplitudes.isEmpty()) return@Canvas

        val barWidth = size.width / amplitudes.size
        val centerY = size.height / 2

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2
            val barHeight = amplitude * size.height * 0.6f

            drawLine(
                color = color.copy(alpha = 0.7f),
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = barWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}

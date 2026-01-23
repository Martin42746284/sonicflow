package com.example.sonicflow.domain.usecase.waveform

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Classe helper pour les calculs de waveform
 */
object WaveformHelper {

    /**
     * Génère une waveform de test (onde sinusoïdale)
     * Utile pour le développement et les tests
     */
    fun generateSineWave(
        samplesCount: Int = 100,
        frequency: Float = 2f,
        amplitude: Float = 0.8f
    ): List<Float> {
        return List(samplesCount) { index ->
            val normalized = index.toFloat() / samplesCount
            val value = sin(normalized * frequency * 2 * Math.PI).toFloat()
            abs(value) * amplitude
        }
    }

    /**
     * Génère une waveform de test (forme triangulaire)
     */
    fun generateTriangleWave(
        samplesCount: Int = 100,
        frequency: Float = 2f,
        amplitude: Float = 0.8f
    ): List<Float> {
        return List(samplesCount) { index ->
            val normalized = index.toFloat() / samplesCount
            val phase = (normalized * frequency) % 1f
            val value = if (phase < 0.5f) phase * 2 else (1f - phase) * 2
            value * amplitude
        }
    }

    /**
     * Calcule l'énergie RMS (Root Mean Square) d'une portion de waveform
     */
    fun calculateRMS(waveformData: List<Float>): Float {
        if (waveformData.isEmpty()) return 0f

        val sumOfSquares = waveformData.map { it * it }.sum()
        return sqrt(sumOfSquares / waveformData.size)
    }

    /**
     * Détecte les pics dans la waveform
     * Retourne les indices des pics locaux
     */
    fun detectPeaks(
        waveformData: List<Float>,
        threshold: Float = 0.7f
    ): List<Int> {
        if (waveformData.size < 3) return emptyList()

        val peaks = mutableListOf<Int>()

        for (i in 1 until waveformData.size - 1) {
            val current = waveformData[i]
            val prev = waveformData[i - 1]
            val next = waveformData[i + 1]

            if (current > threshold && current > prev && current > next) {
                peaks.add(i)
            }
        }

        return peaks
    }

    /**
     * Convertit les amplitudes en décibels
     */
    fun amplitudeToDecibels(amplitude: Float): Float {
        if (amplitude <= 0f) return -96f // Silence
        return 20f * log10(amplitude)
    }

    /**
     * Convertit les décibels en amplitude
     */
    fun decibelsToAmplitude(decibels: Float): Float {
        return 10.0.pow((decibels / 20.0)).toFloat()
    }
}
package com.example.sonicflow.domain.usecase.waveform

import com.example.sonicflow.domain.repository.MediaRepository
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

class GenerateWaveformUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val trackRepository: TrackRepository
) {
    /**
     * Génère les données de waveform pour une piste audio
     * Version simplifiée qui retourne directement le String (pour PlayerViewModel)
     *
     * @param audioPath Chemin du fichier audio
     * @param samplesCount Nombre d'échantillons à générer (défaut: 100)
     * @return Les données de waveform en JSON
     */
    suspend operator fun invoke(
        audioPath: String,
        samplesCount: Int = 100
    ): String = withContext(Dispatchers.IO) {
        try {
            val waveformData = mediaRepository.generateWaveformData(audioPath, samplesCount)
            waveformData ?: "[]"
        } catch (e: Exception) {
            "[]" // Retourne un tableau vide en cas d'erreur
        }
    }

    /**
     * Génère et sauvegarde la waveform pour une piste
     * Version complète avec sauvegarde en base de données
     *
     * @param trackId ID de la piste
     * @param audioPath Chemin du fichier audio
     * @param samplesCount Nombre d'échantillons à générer (défaut: 100)
     * @return Result avec les données de waveform
     */
    suspend fun generateAndSave(
        trackId: Long,
        audioPath: String,
        samplesCount: Int = 100
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Vérifier si la waveform existe déjà
            val track = trackRepository.getTrackById(trackId)
            if (track?.hasWaveform() == true) {
                return@withContext Result.success(track.waveformData!!)
            }

            // Générer la waveform
            val waveformData = mediaRepository.generateWaveformData(audioPath, samplesCount)

            if (waveformData != null) {
                // Sauvegarder dans la base de données
                trackRepository.updateWaveformData(trackId, waveformData)
                Result.success(waveformData)
            } else {
                Result.failure(Exception("Failed to generate waveform data"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Génère la waveform sans la sauvegarder
     * Utile pour prévisualisation ou test
     */
    suspend fun generateWithoutSaving(
        audioPath: String,
        samplesCount: Int = 100
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val waveformData = mediaRepository.generateWaveformData(audioPath, samplesCount)

            if (waveformData != null) {
                Result.success(waveformData)
            } else {
                Result.failure(Exception("Failed to generate waveform data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse les données JSON de waveform en liste de Float
     * @param waveformJson Données JSON de la waveform
     * @return Liste des amplitudes (0.0 - 1.0)
     */
    fun parseWaveformData(waveformJson: String): List<Float> {
        return try {
            val jsonArray = JSONArray(waveformJson)
            List(jsonArray.length()) { index ->
                jsonArray.getDouble(index).toFloat()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Génère des données de waveform par défaut (pour les pistes sans waveform)
     * Crée une forme d'onde aléatoire simple
     */
    fun generateDefaultWaveform(samplesCount: Int = 100): List<Float> {
        return List(samplesCount) {
            (0.3f + Math.random().toFloat() * 0.7f) // Valeurs entre 0.3 et 1.0
        }
    }

    /**
     * Rééchantillonne les données de waveform à une nouvelle taille
     * Utile pour adapter la waveform à différentes tailles d'écran
     */
    fun resampleWaveform(
        waveformData: List<Float>,
        targetSize: Int
    ): List<Float> {
        if (waveformData.isEmpty()) return emptyList()
        if (waveformData.size == targetSize) return waveformData

        val result = mutableListOf<Float>()
        val step = waveformData.size.toFloat() / targetSize

        for (i in 0 until targetSize) {
            val index = (i * step).toInt().coerceIn(0, waveformData.size - 1)
            result.add(waveformData[index])
        }

        return result
    }

    /**
     * Calcule l'amplitude moyenne d'une portion de la waveform
     * Utile pour afficher une version simplifiée
     */
    fun getAverageAmplitude(
        waveformData: List<Float>,
        startIndex: Int,
        endIndex: Int
    ): Float {
        if (waveformData.isEmpty() || startIndex >= endIndex) return 0f

        val validStart = startIndex.coerceIn(0, waveformData.size - 1)
        val validEnd = endIndex.coerceIn(validStart, waveformData.size)

        val slice = waveformData.subList(validStart, validEnd)
        return slice.average().toFloat()
    }

    /**
     * Normalise les données de waveform entre 0 et 1
     */
    fun normalizeWaveform(waveformData: List<Float>): List<Float> {
        if (waveformData.isEmpty()) return emptyList()

        val max = waveformData.maxOrNull() ?: 1f
        if (max == 0f) return waveformData

        return waveformData.map { it / max }
    }

    /**
     * Lisse les données de waveform pour un affichage plus fluide
     * Applique une moyenne mobile
     */
    fun smoothWaveform(
        waveformData: List<Float>,
        windowSize: Int = 3
    ): List<Float> {
        if (waveformData.size < windowSize) return waveformData

        val smoothed = mutableListOf<Float>()
        val halfWindow = windowSize / 2

        for (i in waveformData.indices) {
            val start = (i - halfWindow).coerceAtLeast(0)
            val end = (i + halfWindow + 1).coerceAtMost(waveformData.size)
            val average = waveformData.subList(start, end).average().toFloat()
            smoothed.add(average)
        }

        return smoothed
    }

    /**
     * Calcule la position dans la waveform basée sur le pourcentage de lecture
     * @param progress Progression de 0.0 à 1.0
     * @param waveformSize Nombre d'échantillons de la waveform
     * @return Index correspondant dans la waveform
     */
    fun getWaveformIndexFromProgress(progress: Float, waveformSize: Int): Int {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return (clampedProgress * (waveformSize - 1)).toInt()
    }

    /**
     * Vérifie si les données de waveform sont valides
     */
    fun isValidWaveformData(waveformJson: String?): Boolean {
        if (waveformJson.isNullOrBlank()) return false

        return try {
            val parsed = parseWaveformData(waveformJson)
            parsed.isNotEmpty() && parsed.all { it in 0f..1f }
        } catch (e: Exception) {
            false
        }
    }
}
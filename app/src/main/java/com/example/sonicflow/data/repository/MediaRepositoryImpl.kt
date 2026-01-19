package com.example.sonicflow.data.repository

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import com.example.sonicflow.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import kotlin.math.abs

class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {

    override suspend fun generateWaveformData(audioPath: String, samplesCount: Int): String? = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioPath)

            // Find audio track
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                extractor.release()
                return@withContext null
            }

            extractor.selectTrack(audioTrackIndex)

            val amplitudes = mutableListOf<Float>()
            val bufferSize = 4096
            val buffer = java.nio.ByteBuffer.allocate(bufferSize)

            // Read audio samples
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                // Calculate amplitude from buffer
                var sum = 0f
                buffer.rewind()
                for (i in 0 until minOf(sampleSize, bufferSize) step 2) {
                    val sample = buffer.short.toFloat()
                    sum += abs(sample)
                }

                val amplitude = sum / (sampleSize / 2)
                amplitudes.add(amplitude)

                extractor.advance()
                buffer.clear()
            }

            extractor.release()

            // Downsample to desired samples count
            val downsampled = downsampleAmplitudes(amplitudes, samplesCount)

            // Normalize amplitudes to 0-1 range
            val maxAmplitude = downsampled.maxOrNull() ?: 1f
            val normalized = downsampled.map { it / maxAmplitude }

            // Convert to JSON
            val jsonArray = JSONArray()
            normalized.forEach { jsonArray.put(it) }
            jsonArray.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun downsampleAmplitudes(amplitudes: List<Float>, targetSize: Int): List<Float> {
        if (amplitudes.size <= targetSize) return amplitudes

        val result = mutableListOf<Float>()
        val step = amplitudes.size.toFloat() / targetSize

        for (i in 0 until targetSize) {
            val index = (i * step).toInt()
            result.add(amplitudes[index])
        }

        return result
    }

    override suspend fun extractAudioDuration(audioPath: String): Long = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioPath)

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    val duration = format.getLong(MediaFormat.KEY_DURATION)
                    extractor.release()
                    return@withContext duration / 1000 // Convert to milliseconds
                }
            }

            extractor.release()
            0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}
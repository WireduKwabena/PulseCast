package com.masterminds.pulsecast.core

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class ExportOptions(
    val inputUri: Uri,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val targetAspectRatio: String = "16:9", // "16:9", "9:16", "1:1"
    val speedMultiplier: Float = 1.0f,
    val outputResolutionHeight: Int = 1080 // 720, 1080, 2160
)

data class ExportProgress(
    val percentage: Int = 0,
    val isCompleted: Boolean = false,
    val outputFile: File? = null,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class Media3VideoExporter(private val context: Context) {

    private val _progressState = MutableStateFlow(ExportProgress())
    val progressState: StateFlow<ExportProgress> = _progressState.asStateFlow()

    private var currentTransformer: Transformer? = null

    suspend fun exportClip(options: ExportOptions): File = withContext(Dispatchers.Main) {
        _progressState.value = ExportProgress(percentage = 0, isCompleted = false)

        val outputDir = File(context.getExternalFilesDir(null), "Exports").apply { mkdirs() }
        val outputFile = File(outputDir, "PulseClip_${UUID.randomUUID().toString().take(8)}.mp4")

        // 1. Clipping Configuration
        val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
        if (options.startMs > 0) clippingBuilder.setStartPositionMs(options.startMs)
        if (options.endMs > options.startMs) clippingBuilder.setEndPositionMs(options.endMs)

        val mediaItem = MediaItem.Builder()
            .setUri(options.inputUri)
            .setClippingConfiguration(clippingBuilder.build())
            .build()

        // 2. Video Effects (Aspect Ratio / Scale Transformation)
        val videoEffects = mutableListOf<Effect>()
        when (options.targetAspectRatio) {
            "9:16" -> {
                videoEffects.add(
                    ScaleAndRotateTransformation.Builder()
                        .setScale(0.5625f, 1.0f)
                        .build()
                )
            }
            "1:1" -> {
                videoEffects.add(
                    ScaleAndRotateTransformation.Builder()
                        .setScale(0.75f, 0.75f)
                        .build()
                )
            }
        }

        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(emptyList(), videoEffects))
            .build()

        // 3. Build Transformer Pipeline
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    _progressState.value = ExportProgress(
                        percentage = 100,
                        isCompleted = true,
                        outputFile = outputFile
                    )
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    _progressState.value = ExportProgress(
                        percentage = 0,
                        isCompleted = false,
                        errorMessage = exportException.localizedMessage ?: "Transcoding failed"
                    )
                }
            })
            .build()

        currentTransformer = transformer
        transformer.start(editedMediaItem, outputFile.absolutePath)

        outputFile
    }

    fun cancelExport() {
        currentTransformer?.cancel()
        _progressState.value = ExportProgress(errorMessage = "Export canceled")
    }
}

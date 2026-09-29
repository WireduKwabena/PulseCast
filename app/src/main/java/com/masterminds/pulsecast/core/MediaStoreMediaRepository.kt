package com.masterminds.pulsecast.core

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VaultMediaItem(
    val id: Long,
    val contentUri: Uri,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String,
    val formattedDate: String,
    val mimeType: String,
    val isClip: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSec = durationMs / 1000
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remMinutes = minutes % 60
                String.format(Locale.US, "%02d:%02d:%02d", hours, remMinutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024.0) {
                String.format(Locale.US, "%.1f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }
}

data class StorageTelemetry(
    val usedGb: Double,
    val totalGb: Double,
    val freeGb: Double,
    val usedPercentage: Float
)

class MediaStoreMediaRepository(private val context: Context) {

    suspend fun queryStudioMedia(): List<VaultMediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<VaultMediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)

            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "PulseCast_Recording"
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val height = cursor.getInt(heightColumn)
                val dateSec = cursor.getLong(dateColumn)
                val mime = cursor.getString(mimeColumn) ?: "video/mp4"

                val contentUri = ContentUris.withAppendedId(collection, id)

                val resText = when {
                    height >= 2160 -> "4K 60fps"
                    height >= 1440 -> "2K 60fps"
                    height >= 1080 -> "1080p60"
                    height >= 720 -> "720p60"
                    else -> "1080p60"
                }

                val dateStr = dateFormat.format(Date(dateSec * 1000))
                val isClip = duration in 1..60000 // Under 60 sec is a clip

                mediaList.add(
                    VaultMediaItem(
                        id = id,
                        contentUri = contentUri,
                        title = name.removeSuffix(".mp4"),
                        durationMs = duration,
                        sizeBytes = size,
                        resolution = resText,
                        formattedDate = dateStr,
                        mimeType = mime,
                        isClip = isClip
                    )
                )
            }
        }

        mediaList
    }

    fun getStorageTelemetry(): StorageTelemetry {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val usedBytes = totalBytes - availableBytes

        val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
        val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
        val freeGb = availableBytes / (1024.0 * 1024.0 * 1024.0)
        val percentage = (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

        return StorageTelemetry(usedGb, totalGb, freeGb, percentage)
    }

    fun createBatchDeleteIntent(uris: List<Uri>): PendingIntent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return MediaStore.createDeleteRequest(context.contentResolver, uris)
        }
        return null
    }

    suspend fun deleteDirect(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createShareIntent(uri: Uri, mimeType: String = "video/mp4"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

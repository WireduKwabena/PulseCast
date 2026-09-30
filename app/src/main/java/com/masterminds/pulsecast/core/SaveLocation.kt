package com.masterminds.pulsecast.core

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.File
import java.io.FileDescriptor
import java.io.Closeable
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Where a finished recording gets written. Two genuinely different code
 * paths, not a cosmetic split:
 *
 * - Android 10+ (API 29+): scoped storage is mandatory. Writing straight to
 *   a File in the public Movies directory either throws or silently fails
 *   depending on OEM, which is a real, common source of the "recording
 *   completed but the file's missing" complaints XRecorder-class apps get.
 *   The correct approach is inserting a MediaStore.Video entry and writing
 *   through the FileDescriptor it hands back.
 *
 * - Android 9 and below: MediaStore's video collection insert path isn't
 *   reliable pre-scoped-storage, so this falls back to a direct File in the
 *   public Movies directory (requires the maxSdkVersion=28-scoped
 *   WRITE_EXTERNAL_STORAGE permission declared in the manifest).
 */
class SaveLocation private constructor(
    val fileDescriptor: FileDescriptor,
    val displayName: String,
    private val mediaStoreUri: Uri?,
    private val context: Context,
    private val descriptorOwner: Closeable,
    private val legacyFile: File? = null,
) {
    private var closed = false

    fun generateName(): String = displayName

    /** Close the owned descriptor and publish the completed recording to other apps. */
    @Synchronized
    fun complete() {
        if (closed) return
        runCatching { descriptorOwner.close() }
        val uri = mediaStoreUri
        if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
            runCatching { context.contentResolver.update(uri, values, null, null) }
        }
        closed = true
    }

    /** Remove an incomplete output after startup or muxing failure. Safe to call more than once. */
    @Synchronized
    fun abort() {
        if (closed) return
        runCatching { descriptorOwner.close() }
        val uri = mediaStoreUri
        if (uri != null) {
            runCatching { context.contentResolver.delete(uri, null, null) }
        } else {
            runCatching { legacyFile?.delete() }
        }
        closed = true
    }

    companion object {
        private val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)

        fun create(context: Context): SaveLocation {
            val displayName = "Recording_${nameFormat.format(Date())}.mp4"

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/ScreenRecorder")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("MediaStore rejected the insert — device storage may be full or unavailable")
                try {
                    val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
                        ?: error("Could not open a file descriptor for $uri")
                    SaveLocation(pfd.fileDescriptor, displayName, uri, context, pfd)
                } catch (error: Throwable) {
                    runCatching { context.contentResolver.delete(uri, null, null) }
                    throw error
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ScreenRecorder")
                check(dir.exists() || dir.mkdirs()) { "Could not create recording directory ${dir.absolutePath}" }
                val file = File(dir, displayName)
                val stream = FileOutputStream(file)
                SaveLocation(stream.fd, displayName, null, context, stream, file)
            }
        }
    }
}

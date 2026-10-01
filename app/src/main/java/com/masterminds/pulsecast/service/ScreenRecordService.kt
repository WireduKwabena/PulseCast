package com.masterminds.pulsecast.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.masterminds.pulsecast.MainActivity
import com.masterminds.pulsecast.core.*
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.core.AudioCaptureConfig
import com.masterminds.pulsecast.encoder.AudioEncoder
import com.masterminds.pulsecast.encoder.MuxerSink
import com.masterminds.pulsecast.encoder.MuxerWrapper
import com.masterminds.pulsecast.encoder.VideoEncoder
import com.masterminds.pulsecast.streaming.DestinationConnection
import com.masterminds.pulsecast.streaming.EncodedSampleSink
import com.masterminds.pulsecast.streaming.RootEncoderRtmpPublisher
import com.masterminds.pulsecast.streaming.RtmpDestinationSink
import com.masterminds.pulsecast.streaming.StreamDestination
import com.masterminds.pulsecast.core.RecordingEvent
import com.masterminds.pulsecast.core.RecordingStateMachine
import com.masterminds.pulsecast.core.SaveLocation
import com.masterminds.pulsecast.core.CaptureSessionStore
import kotlinx.coroutines.*

class ScreenRecordService : Service() {

    companion object {
        const val ACTION_START = "com.masterminds.pulsecast.action.START"
        const val ACTION_PAUSE = "com.masterminds.pulsecast.action.PAUSE"
        const val ACTION_RESUME = "com.masterminds.pulsecast.action.RESUME"
        const val ACTION_STOP = "com.masterminds.pulsecast.action.STOP"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        // Parallel arrays rather than a Parcelable list — keeps
        // StreamDestination itself free of any Android dependency, so it
        // stays usable from the pure-Kotlin streaming/ test suite.
        const val EXTRA_STREAM_LABELS = "streamLabels"
        const val EXTRA_STREAM_IDS = "streamIds"
        const val EXTRA_STREAM_URLS = "streamUrls"
        const val EXTRA_STREAM_BITRATE_KBPS = "streamBitrateKbps"
        const val EXTRA_RESOLUTION = "resolution"
        const val EXTRA_FPS = "fps"
        const val EXTRA_AUDIO_MODE = "audioMode"

        private const val NOTIFICATION_CHANNEL_ID = "recording"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "ScreenRecordService"
    }

    private val stateMachine = RecordingStateMachine()
    private val ptsAdjuster = PtsAdjuster()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var durationJob: Job? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null
    private var muxer: MuxerWrapper? = null
    private var saveLocation: SaveLocation? = null
    private var streamSinks: List<RtmpDestinationSink> = emptyList()
    private var startupSucceeded = false

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in ScreenRecordService on thread ${thread.name}", throwable)
            runCatching { handleStop() }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
            LiveHUDOverlayService.ACTION_MIC_MUTED -> audioEncoder?.setMicrophoneMuted(true)
            LiveHUDOverlayService.ACTION_MIC_UNMUTED -> audioEncoder?.setMicrophoneMuted(false)
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        if (stateMachine.current() != RecordingState.IDLE) {
            Log.w(TAG, "Ignoring duplicate start while recorder is ${stateMachine.current()}")
            return
        }
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        @Suppress("DEPRECATION")
        val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA) ?: run {
            Log.e(TAG, "Missing MediaProjection result data — cannot start recording")
            stopSelf()
            return
        }

        val requestedAudioMode = intent.getStringExtra(EXTRA_AUDIO_MODE) ?: AudioMode.MIC_ONLY.name
        val audioMode = runCatching { AudioMode.valueOf(requestedAudioMode) }.getOrDefault(AudioMode.MIC_ONLY)
        startForeground(NOTIFICATION_ID, buildNotification(), foregroundServiceTypeForStart(audioMode))

        stateMachine.transition(RecordingEvent.Start)

        try {
        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val projection = projectionManager.getMediaProjection(resultCode, resultData) ?: run {
            Log.e(TAG, "Failed to get MediaProjection — cannot start recording")
            stateMachine.transition(RecordingEvent.PermissionDenied)
            stopForeground(STOP_FOREGROUND_REMOVE)
            CaptureSessionStore.setRecording(false)
            stopSelf()
            return
        }
        mediaProjection = projection

        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.w(TAG, "MediaProjection onStop triggered")
                if (stateMachine.isActive()) {
                    handleStop()
                }
            }

            override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                Log.d(TAG, "MediaProjection content visibility changed: $isVisible")
            }

            override fun onCapturedContentResize(width: Int, height: Int) {
                Log.d(TAG, "MediaProjection content resized: $width x $height")
            }
        }, Handler(Looper.getMainLooper()))

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }

        val resolutionLabel = intent.getStringExtra(EXTRA_RESOLUTION) ?: "1080p"
        val fpsValue = intent.getIntExtra(EXTRA_FPS, 60)
        val quality = when (resolutionLabel) {
            "2K", "1440p" -> Quality.HIGH
            "4K" -> Quality.ORIGINAL
            "720p" -> Quality.LOW
            else -> Quality.MEDIUM // 1080p
        }
        val frameRate = fpsValue

        val resolution = ResolutionScaler.scaleToTier(metrics.widthPixels, metrics.heightPixels, quality)
        val requestedStreamBitrateKbps = intent.getIntExtra(EXTRA_STREAM_BITRATE_KBPS, 0)
        val bitRate = requestedStreamBitrateKbps
            .takeIf { it in 500..50_000 }
            ?.times(1_000)
            ?: BitrateCalculator.calculate(resolution, frameRate, quality)

        stateMachine.transition(RecordingEvent.PermissionGranted)

        saveLocation = SaveLocation.create(this)
        val expectedTracks = 2
        muxer = MuxerWrapper(saveLocation!!.fileDescriptor, expectedTracks)
        val muxerSink = MuxerSink(muxer!!)

        // V1 supports one RTMP target. A connection failure must not block
        // the local recording sink, so publishing remains an isolated sink.
        val ids = intent.getStringArrayExtra(EXTRA_STREAM_IDS) ?: emptyArray()
        val labels = intent.getStringArrayExtra(EXTRA_STREAM_LABELS) ?: emptyArray()
        val urls = intent.getStringArrayExtra(EXTRA_STREAM_URLS) ?: emptyArray()
        if (labels.size != urls.size || (ids.isNotEmpty() && ids.size != labels.size)) Log.w(TAG, "Ignoring malformed stream destinations: arrays differ in count")
        streamSinks = (0 until minOf(labels.size, urls.size)).map { i ->
            val destination = StreamDestination(
                id = ids.getOrNull(i) ?: "dest_$i",
                label = labels[i],
                rtmpUrl = urls[i],
                bitrateKbps = requestedStreamBitrateKbps.takeIf { it > 0 }
            )
            RtmpDestinationSink(DestinationConnection(destination), RootEncoderRtmpPublisher()).also { it.start() }
        }

        val allSinks: List<EncodedSampleSink> = listOf(muxerSink) + streamSinks

        videoEncoder = VideoEncoder(resolution, frameRate, bitRate, muxer!!, allSinks, ptsAdjuster).also { it.start() }
        val audioCaptureConfig = AudioCaptureConfig(mode = audioMode)
        audioEncoder = AudioEncoder(
            muxer = muxer!!,
            sinks = allSinks,
            ptsAdjuster = ptsAdjuster,
            config = audioCaptureConfig,
            mediaProjection = projection.takeIf { audioMode != AudioMode.MIC_ONLY }
        ).also { it.start() }

        virtualDisplay = projection.createVirtualDisplay(
            "ScreenRecorder",
            resolution.width,
            resolution.height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            videoEncoder!!.inputSurface,
            null,
            null
        ) ?: throw IllegalStateException("Android did not create the screen capture display")
        startupSucceeded = true
        CaptureSessionStore.setRecording(true)
        CaptureSessionStore.setBroadcasting(streamSinks.any { it.connection.isLive() })

        durationJob?.cancel()
        durationJob = serviceScope.launch {
            var elapsedSec = 0L
            while (isActive) {
                delay(1000)
                if (!ptsAdjuster.isPaused()) {
                    elapsedSec++
                    CaptureSessionStore.updateDurationSeconds(elapsedSec)
                }
            }
        }
        } catch (error: Exception) {
            Log.e(TAG, "Capture pipeline failed during startup", error)
            if (stateMachine.canStop()) {
                handleStop()
            } else if (stateMachine.current() == RecordingState.PERMISSION_REQUESTED) {
                stateMachine.transition(RecordingEvent.PermissionDenied)
                stopForeground(STOP_FOREGROUND_REMOVE)
                runCatching { mediaProjection?.stop() }
                mediaProjection = null
                CaptureSessionStore.setRecording(false)
                stopSelf()
            }
        }
    }

    private fun foregroundServiceTypeForStart(audioMode: AudioMode): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val includesMicrophone = audioMode == AudioMode.MIC_ONLY || audioMode == AudioMode.INTERNAL_AND_MIC
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && includesMicrophone) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
        } else {
            0
        }

    private fun handlePause() {
        if (!stateMachine.canPause()) return
        stateMachine.transition(RecordingEvent.Pause)
        virtualDisplay?.setSurface(null)
        audioEncoder?.pauseCapture()
        ptsAdjuster.onPause(System.nanoTime() / 1000)
    }

    private fun handleResume() {
        if (!stateMachine.canResume()) return
        ptsAdjuster.onResume(System.nanoTime() / 1000)
        audioEncoder?.resumeCapture()
        virtualDisplay?.setSurface(videoEncoder?.inputSurface)
        stateMachine.transition(RecordingEvent.Resume)
    }

    private fun handleStop() {
        if (!stateMachine.canStop()) {
            stopSelf()
            return
        }
        stateMachine.transition(RecordingEvent.Stop)

        runCatching { virtualDisplay?.release() }.onFailure { Log.w(TAG, "Virtual display cleanup failed", it) }
        virtualDisplay = null

        runCatching { videoEncoder?.stop() }.onFailure { Log.w(TAG, "Video encoder cleanup failed", it) }
        videoEncoder = null

        runCatching { audioEncoder?.stop() }.onFailure { Log.w(TAG, "Audio encoder cleanup failed", it) }
        audioEncoder = null

        streamSinks.forEach { sink -> runCatching { sink.stop() }.onFailure { Log.w(TAG, "Stream sink cleanup failed", it) } }

        runCatching { muxer?.release() }.onFailure { Log.w(TAG, "Muxer cleanup failed", it) }
        muxer = null

        runCatching { mediaProjection?.stop() }.onFailure { Log.w(TAG, "Projection cleanup failed", it) }
        mediaProjection = null

        // ALWAYS publish the recording so any frames captured are saved
        runCatching { saveLocation?.complete() }.onFailure { error ->
            Log.e(TAG, "Recording publication failed", error)
        }
        saveLocation = null

        startupSucceeded = false
        durationJob?.cancel()
        durationJob = null

        stateMachine.transition(RecordingEvent.Finished)
        CaptureSessionStore.setRecording(false)
        CaptureSessionStore.setBroadcasting(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (stateMachine.isActive()) {
            handleStop()
        } else {
            runCatching { saveLocation?.complete() }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Screen recording",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Recording your screen")
            .setContentText("Tap to return to the app")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }
}

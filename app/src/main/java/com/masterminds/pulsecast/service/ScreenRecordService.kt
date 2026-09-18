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
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.masterminds.pulsecast.MainActivity
import com.masterminds.pulsecast.core.*
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
        const val EXTRA_STREAM_URLS = "streamUrls"

        private const val NOTIFICATION_CHANNEL_ID = "recording"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "ScreenRecordService"
    }

    private val stateMachine = RecordingStateMachine()
    private val ptsAdjuster = PtsAdjuster()

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null
    private var muxer: MuxerWrapper? = null
    private var saveLocation: SaveLocation? = null
    private var streamSinks: List<RtmpDestinationSink> = emptyList()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        @Suppress("DEPRECATION")
        val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA) ?: run {
            Log.e(TAG, "Missing MediaProjection result data — cannot start recording")
            stopSelf()
            return
        }

        startForeground(NOTIFICATION_ID, buildNotification(), foregroundServiceTypeForStart())

        stateMachine.transition(RecordingEvent.Start)

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val projection = projectionManager.getMediaProjection(resultCode, resultData) ?: run {
            Log.e(TAG, "Failed to get MediaProjection — cannot start recording")
            stopSelf()
            return
        }
        mediaProjection = projection

        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.w(TAG, "MediaProjection stopped by the system — finishing the recording gracefully")
                handleStop()
            }
        }, null)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }
        val resolution = ResolutionScaler.scaleToTier(metrics.widthPixels, metrics.heightPixels, Quality.MEDIUM)
        val frameRate = 30
        val bitRate = BitrateCalculator.calculate(resolution, frameRate, Quality.MEDIUM)

        stateMachine.transition(RecordingEvent.PermissionGranted)

        saveLocation = SaveLocation.create(this)
        val expectedTracks = 2
        muxer = MuxerWrapper(saveLocation!!.fileDescriptor, expectedTracks)
        val muxerSink = MuxerSink(muxer!!)

        // Phase 3: build one RtmpDestinationSink per requested stream
        // target, and start each connecting immediately. Each is
        // independent — see DestinationConnection's own isolation
        // guarantee — so one destination failing to connect (very likely
        // right now, since RootEncoderRtmpPublisher isn't fully wired to
        // the real library yet) never blocks local recording or any other
        // destination.
        val labels = intent.getStringArrayExtra(EXTRA_STREAM_LABELS) ?: emptyArray()
        val urls = intent.getStringArrayExtra(EXTRA_STREAM_URLS) ?: emptyArray()
        streamSinks = labels.indices.map { i ->
            val destination = StreamDestination(id = "dest_$i", label = labels[i], rtmpUrl = urls[i])
            RtmpDestinationSink(DestinationConnection(destination), RootEncoderRtmpPublisher()).also { it.start() }
        }

        val allSinks: List<EncodedSampleSink> = listOf(muxerSink) + streamSinks

        videoEncoder = VideoEncoder(resolution, frameRate, bitRate, muxer!!, allSinks, ptsAdjuster).also { it.start() }
        audioEncoder = AudioEncoder(muxer!!, allSinks, ptsAdjuster).also { it.start() }

        virtualDisplay = projection.createVirtualDisplay(
            "ScreenRecorder",
            resolution.width,
            resolution.height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            videoEncoder!!.inputSurface,
            null,
            null
        )
    }

    private fun foregroundServiceTypeForStart(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        } else {
            0
        }

    private fun handlePause() {
        if (!stateMachine.canPause()) return
        stateMachine.transition(RecordingEvent.Pause)
        ptsAdjuster.onPause(System.nanoTime() / 1000)
    }

    private fun handleResume() {
        if (!stateMachine.canResume()) return
        ptsAdjuster.onResume(System.nanoTime() / 1000)
        stateMachine.transition(RecordingEvent.Resume)
    }

    private fun handleStop() {
        if (!stateMachine.canStop()) {
            stopSelf()
            return
        }
        stateMachine.transition(RecordingEvent.Stop)

        virtualDisplay?.release()
        videoEncoder?.stop()
        audioEncoder?.stop()
        muxer?.release()
        streamSinks.forEach { it.stop() }
        mediaProjection?.stop()
        saveLocation?.finalize()

        stateMachine.transition(RecordingEvent.Finished)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (stateMachine.isActive()) handleStop()
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

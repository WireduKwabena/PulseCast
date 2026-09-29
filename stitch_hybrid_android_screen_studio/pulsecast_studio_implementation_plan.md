# PulseCast Studio — Android Technical Implementation Plan & Architecture Specification

This engineering specification outlines the full native Android technical architecture, APIs, background services, hardware acceleration pipelines, and screen-by-screen functionality implementation for **PulseCast Studio** (blending XRecorder & Prism Live Studio).

---

## 1. System Architecture Overview & Core Android Subsystems

### A. Core Framework Technologies
- **Language**: Kotlin 1.9+ with Coroutines & StateFlow / SharedFlow.
- **Architecture**: Clean Architecture + MVI / MVVM with Jetpack Compose / Jetpack Navigation & Android Services.
- **Screen Capture Engine**: Android `MediaProjectionManager`, `VirtualDisplay`, `Surface`, and `MediaCodec` (hardware accelerated AVC / HEVC / AV1).
- **Audio Capture & DSP Engine**: 
  - `AudioPlaybackCaptureConfiguration` (Android 10+ / API 29+) for internal audio loopback.
  - Oboe (C++ high-performance audio engine) or AAudio for low-latency mic capture, real-time noise suppression, dynamic range compression (DRC), and software stereo mixer.
  - Multi-track audio routing (`AudioRecord` + Dual virtual buses for DMCA isolation).
- **Video Processing & Compositions**:
  - OpenGL ES 3.0 / Vulkan shaders for real-time camera PIP masking (Circle, Squircle, Hexagon), chroma key subtraction, and stream overlay blending (alerts, chat, watermarks).
  - FFmpeg (libavcodec, libavformat, libavfilter) for timeline video editing, fast remuxing, and clip rendering.
- **Network Ingest & Multistream Engine**:
  - Native C/C++ RTMP library (e.g., `librtmp` or SRS `srs-librtmp`) and `libsrt` (Secure Reliable Transport) with hardware-paced TS/FLV muxing.
  - Socket pooling and non-blocking asynchronous multiplexing for broadcasting to multiple RTMP/SRT ingest nodes concurrently without re-encoding video.
- **Floating Assistive Touch Overlay**:
  - Android `WindowManager` with `TYPE_APPLICATION_OVERLAY`, custom physics touch interceptor (`VelocityTracker`, `DynamicAnimation.SpringAnimation`), and hardware acceleration layers.
- **System Background Management**:
  - Android Foreground Service with `foregroundServiceType="mediaProjection|camera|microphone"`.
  - Android 14+ (API 34) compliant runtime permissions and notification channel management.

---

## 2. Screen-by-Screen Functional Implementation Plan

---

### 1. Capture Hub (Home Recording Dashboard)

#### Functional Scope:
High-FPS screen capture initialization, configuration persistency, storage telemetry polling, and instant recording trigger.

#### Implementation Architecture & APIs:
1. **MediaProjection Intent Handshake**:
   - Register an `ActivityResultLauncher<Intent>` via `registerForActivityResult(StartActivityForResult())` calling `mediaProjectionManager.createScreenCaptureIntent()`.
   - On consent, pass the grant token to `ScreenRecorderService` via Android Foreground Service (`startForegroundService()`).
2. **Audio Mode Bus Selection**:
   - `Internal Only`: Build `AudioPlaybackCaptureConfiguration.Builder(mediaProjection).addMatchingUsage(AudioAttributes.USAGE_GAME).build()` feeding `AudioRecord`.
   - `Mic Only`: Standard `AudioSource.VOICE_COMMUNICATION` with system echo cancellation (`AcousticEchoCanceler`).
   - `Internal + Mic (Mixed)`: Dual `AudioRecord` threads routed through an Oboe DSP mixer thread performing floating-point PCM summation with soft clipping prevention.
3. **Capture Profile Configuration (`1080p`, `2K`, `4K` / `60 FPS`, `120 FPS`)**:
   - Query `DisplayMetrics` and `MediaCodecList` to verify encoder profile level support (`AVCProfileHigh`, `HEVCProfileMain`).
   - For 120 FPS capture, query `Display.getSupportedModes()` to verify high refresh rate display capabilities and configure `MediaFormat.KEY_FRAME_RATE` + `KEY_CAPTURE_RATE`.
4. **Storage Telemetry Engine**:
   - Use `StatFs(Environment.getDataDirectory().path)` to compute `availableBytes` and calculate remaining recording duration based on active profile bitrate: `remainingMinutes = availableBytes / (bitrateMbps * 125000 * 60)`.
   - Poll every 10 seconds via Kotlin Coroutine Flow with lifecycle-aware collection.
5. **Countdown & Launch Workflow**:
   - 3-second countdown overlay rendered via lightweight system window or in-app countdown animation.
   - App calls `moveTaskToBack(true)` to minimize cleanly and triggers `FloatingOverlayService` to display the in-game HUD.

---

### 2. Live Multistream Studio (Broadcast Command Center)

#### Functional Scope:
Multi-destination broadcasting setup, hardware encoder orchestration, parallel stream dispatch, and camera PIP preview.

#### Implementation Architecture & APIs:
1. **Parallel Stream Ingest Pipeline**:
   - Single Encoder, Multiple Sinks: The `MediaCodec` encodes the GPU-composited surface once (H.264/HEVC NAL units).
   - Ingest Dispatcher: A native C++ worker thread receives encoded NAL units and muxes them into multiple parallel socket connections:
     - YouTube RTMP (`rtmp://a.rtmp.youtube.com/live2`)
     - Twitch Ingest (`rtmp://live.twitch.tv/app/`)
     - Kick RTMP (`rtmps://fa723794b6f0.global-contribute.live-video.net`)
     - TikTok RTMP or Custom SRT Caller.
   - Handles network jitter independently: If one platform socket throttles, its buffer drops non-keyframes (P/B-frame dropping) without degrading or lagging other stream outputs.
2. **Camera PIP Live Preview**:
   - Android `CameraX` (`PreviewView` + `ImageAnalysis`) targeting front camera.
   - SurfaceTexture bound to an OpenGL ES texture ID, passed to the master rendering pipeline for composition.
3. **Dynamic Follower Count & Channel Polling**:
   - Authenticated REST / WebSocket APIs for YouTube Data API v3 (`channels.list`), Twitch Helix API (`users/follows`), and Kick API.
4. **Encoder Configuration Bridge**:
   - Exposes shared state to `Broadcast Setup & RTMP Studio` and `Pre-Stream Checklist`.

---

### 3. Pre-Stream Go-Live Safety Checklist

#### Functional Scope:
Automated pre-flight diagnostic hardware polling, DND mode detection and activation, unlisted rehearsal testing.

#### Implementation Architecture & APIs:
1. **Diagnostic Polling Suite**:
   - **Battery & Thermal Headroom**: Register `BroadcastReceiver` for `Intent.ACTION_BATTERY_CHANGED`. Query `BatteryManager.EXTRA_LEVEL` and `EXTRA_STATUS`. Query `PowerManager.thermalStatus` (requires API 29+ `getThermalStatus()`) to verify device is below `THERMAL_STATUS_SEVERE`.
   - **Network Uplink & Jitter**: Initiate an active 5-second socket probe sending synthetic payload to the primary ingest node. Measure Round Trip Time (RTT), jitter variance, and bandwidth throughput via Android `ConnectivityManager` (`NetworkCapabilities.TRANSPORT_WIFI`).
   - **Mic Decibel Level Metering**: Run a 16-bit PCM buffer on `AudioRecord`, computing Root Mean Square (RMS):
     $$dB = 20 \cdot \log_{10}\left(\frac{\text{RMS}}{32767}\right)$$
     Emit values continuously to the UI meter.
   - **Privacy Mask Engine Check**: Verify `WindowManager.LayoutParams.FLAG_SECURE` detection hooks for password fields.
2. **Do Not Disturb (DND) Shield Integration**:
   - Check status using `NotificationManager.isNotificationPolicyAccessGranted()`.
   - If ungranted, fire `Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)`.
   - When granted, invoke `notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)` to suppress incoming call banners and chat popups during the broadcast.
3. **Private Rehearsal Stream Mode**:
   - For YouTube: Sets privacy status via YouTube Live API to `"unlisted"` or `"private"`.
   - For Twitch / Custom RTMP: Enables `"test=true"` stream key parameter or streams strictly to a local loopback SRT socket for offline validation.

---

### 4. Live In-Game HUD & Overlay (Assistive In-Game Layer)

#### Functional Scope:
Zero-latency system floating overlay, gesture recognition, 360° radial menu, on-screen telestrator canvas, live chat ticker.

#### Implementation Architecture & APIs:
1. **WindowManager Floating Overlay Lifecycle**:
   - Managed via a dedicated `FloatingOverlayService` using `WindowManager.LayoutParams`:
     - Type: `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
     - Flags: `FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS | FLAG_HARDWARE_ACCELERATED`.
     - Format: `PixelFormat.TRANSLUCENT`.
2. **Touch Interceptor & Magnetic Snapping Physics**:
   - Custom `OnTouchListener` implementing `GestureDetector` and `VelocityTracker`.
   - Detects click vs. drag vs. double-tap vs. long-press (threshold: 800ms).
   - On finger release, use AndroidX `DynamicAnimation` (`SpringAnimation(view, DynamicAnimation.TRANSLATION_X)`) to snap the orb to the nearest left or right display edge with configurable spring stiffness and damping.
3. **360° Radial Menu Engine**:
   - On single tap: Renders a vector-drawn radial canvas computing circular coordinates:
     $$x = r \cdot \cos(\theta), \quad y = r \cdot \sin(\theta)$$
   - Dispatches events to sub-modules (Facecam PIP toggle, doodle mode, soundboard).
4. **Instant 30s AI Replay Buffer (Double Tap)**:
   - Maintains a rolling ring buffer of compressed H.264 video chunks (GOP aligned) and AAC audio in memory using a custom FIFO `MediaMuxer` circular queue.
   - On double tap, commits the last 30 seconds from RAM to disk asynchronously without interrupting active recording or broadcasting.
5. **Telestrator / Drawing Canvas Overlay**:
   - Transparent fullscreen surface with Android `Path` rendering and hardware-accelerated `Canvas`.
   - Captures raw stylus / touch events with custom stroke smoothing (`QuadTo` bezier curves), color selector, and instant clear/undo.

---

### 5. Stream Live Chat & Unified Moderation Drawer

#### Functional Scope:
Multi-platform real-time chat aggregation, WebSocket feeds, inline bot actions, pinned donation alerts, chat velocity analytics.

#### Implementation Architecture & APIs:
1. **Unified WebSocket Multi-Platform Ingestion**:
   - **Twitch**: IRC WebSocket client connecting to `wss://irc-ws.chat.twitch.tv:443`, parsing standard IRC tags (`emotes`, `badges`, `subscriber`).
   - **YouTube Live**: Long-polling or WebSub/PubSub streaming via YouTube Live Streaming API (`liveChatMessages/list`).
   - **Kick**: Pusher WebSocket protocol client connecting to Kick live channel websocket clusters.
   - All inbound events map to a unified Kotlin `ChatMessage` domain model:
     ```kotlin
     data class ChatMessage(
         val id: String,
         val platform: PlatformType,
         val sender: String,
         val content: String,
         val badges: List<Badge>,
         val isSuperChat: Boolean,
         val tipAmount: Double?,
         val timestamp: Long
     )
     ```
2. **Chat Velocity Calculator**:
   - Sliding 60-second window tracking received messages count; recalculates velocity metric every 2 seconds.
3. **Host Moderation Action Execution**:
   - Inline Ban / Timeout: Dispatches API mutations to the respective channel platform via OAuth2 tokens.
   - Live Room Shields: Controls chat slow-mode timers and sub-only chat filters using REST mutations.
4. **Pinned Super Chat Floating Stinger**:
   - High-priority StateFlow triggering a countdown timer (`CountDownTimer`) and emitting an overlay stinger event to the OpenGL composition pipeline.

---

### 6. Floating Ball Customization & Gesture Binder

#### Functional Scope:
Customization engine for floating assistive touch orb, drag-to-dock physics simulator, gesture re-mapping, radial slot assignment.

#### Implementation Architecture & APIs:
1. **Real-Time Physics Simulator**:
   - Embedded interactive sandbox view matching the exact touch interceptor algorithms of the system overlay.
   - Live preview of opacity fading: Evaluates idle decay using a coroutine delay; transitions alpha from active (`100%`) to idle (`35%`) using `ValueAnimator`.
2. **Gesture Key-Action Mapping Persistence**:
   - Android Jetpack DataStore (Preferences or Proto) storing serialized gesture bindings:
     - `GESTURE_SINGLE_TAP` -> `ACTION_RADIAL_MENU`
     - `GESTURE_DOUBLE_TAP` -> `ACTION_AI_CLIP`
     - `GESTURE_LONG_PRESS` -> `ACTION_MIC_MUTE`
     - `GESTURE_SWIPE_IN` -> `ACTION_OPEN_CHAT`
   - Dynamically broadcasts changes via LocalBroadcastManager / SharedFlow to `FloatingOverlayService`.
3. **Radial Slot Reordering**:
   - Drag-and-drop 6-slot circular grid updating the radial command array indices.

---

### 7. Stream Alert & Widget Overlay Studio

#### Functional Scope:
Dynamic layer composition, aspect ratio canvas morphing, donation alerts, dynamic goal tracking, entrance animation rendering.

#### Implementation Architecture & APIs:
1. **OpenGL ES 3.0 Rendering & Composition Engine**:
   - All widgets and overlays are rendered to an off-screen FBO (Frame Buffer Object) texture at broadcast resolution (1920x1080 or 1080x1920).
   - The master rendering thread composites:
     1. Base Layer: Screen capture texture from `VirtualDisplay`.
     2. Second Layer: Camera PIP with active shape shader.
     3. Third Layer: Alert & Widget texture.
     4. Fourth Layer: Telestrator drawing texture.
2. **Aspect Ratio Transition (`16:9` vs `9:16`)**:
   - Reconfigures OpenGL projection matrix (`Matrix.orthoM`) and adjusts `VirtualDisplay` cropping/letterboxing bounds with animated interpolation.
3. **Alert Audio & Soundboard Player**:
   - Android `SoundPool` for ultra-low latency audio playback of selected sound packs (*Cyber Synth*, *8-Bit*, *Epic Brass*).
   - Injects audio buffer directly into the broadcast mix while playing locally through device speakers/headphones.

---

### 8. Pro Multi-Track Audio Mixer & DMCA Shield

#### Functional Scope:
4-channel independent hardware DSP mixing, auto-ducking, master bus limiting, dual-bus DMCA copyright stripping.

#### Implementation Architecture & APIs:
1. **4-Channel Software Audio Mixer (Oboe / C++ Core)**:
   - Channel 1 (Game Capture): From `AudioPlaybackCapture`.
   - Channel 2 (Microphone): From hardware mic, processed with RNNoise (neural noise suppression) and high-pass filtering.
   - Channel 3 (Comms / Discord): Separated via Android 10+ specific `AudioAttributes.USAGE_VOICE_COMMUNICATION` filter.
   - Channel 4 (BGM / Music): From external media player or soundboard.
   - Each channel runs dedicated Gain, Pan (stereo balancing), and Mute controls.
2. **Smart Ducking Compressor**:
   - Side-chain compressor monitoring Mic RMS: When Mic level exceeds -24 dB, automatically attenuates Channel 1 (Game) and Channel 4 (Music) by -40% with 50ms attack and 300ms release curves.
3. **DMCA Shield Dual-Track Routing Matrix**:
   - **Bus A (Live Broadcast)**: Sums all active channels (Game + Mic + Comms + Music) and encodes to AAC Stream 1.
   - **Bus B (VOD Recording)**: Excludes Channel 4 (Music), summing only Game + Mic + Comms into a separate AAC Stream 2.
   - The MP4 container records Dual Audio Tracks (Track 1 = Clean VOD, Track 2 = Full Broadcast), guaranteeing zero copyright strikes on Twitch / YouTube VOD archives.

---

### 9. Instant Clip & Highlight Export Flow

#### Functional Scope:
AI highlight detection integration, precision timeline trimming, 9:16 vertical smart framing, rapid social exporting.

#### Implementation Architecture & APIs:
1. **AI Highlight & Event Marker Snapping**:
   - Reads event timestamps generated during recording (e.g., loud mic reactions, rapid kill streaks, or manual double-tap flags).
   - Auto-sets playhead in/out points around selected markers.
2. **Smart 9:16 Re-Framing Pipeline**:
   - Crops 16:9 landscape video into vertical 9:16.
   - Supports docked Facecam PIP position at top margin with video gameplay anchored below.
3. **Hardware Video Trimming & Remuxing**:
   - Fast Lossless Trim: If cut points fall on Keyframes (IDR frames), uses `MediaExtractor` and `MediaMuxer` for zero-re-encoding instant trim (< 2 seconds).
   - Precision Frame-Accurate Trim: If trimming between GOPs, uses `MediaCodec` decoder/encoder loop with hardware acceleration.
4. **Android Native Share Dispatcher**:
   - FileProvider URI generation (`androidx.core.content.FileProvider`).
   - Dispatches `Intent.ACTION_SEND` targeting specific packages (TikTok, YouTube Shorts, Instagram Reels, Discord).

---

### 10. Custom RTMP & SRT Ingest Node Modal

#### Functional Scope:
Custom transport protocol configuration, SRT handshake negotiation, QR code stream key scanning, real-time socket latency tests.

#### Implementation Architecture & APIs:
1. **SRT (Secure Reliable Transport) Client Implementation**:
   - Links native `libsrt.so` via JNI.
   - Configures SRT Caller socket parameters:
     - `SRTO_LATENCY`: Configurable buffer delay (default: 120ms).
     - `SRTO_PASSPHRASE`: AES-128 / AES-256 stream encryption.
     - `SRTO_STREAMID`: Custom ingest routing key.
2. **QR Code Stream Key Scanner**:
   - CameraX integration with Google ML Kit Barcode Scanning API (`BarcodeScanning.getClient()`).
   - Instantly parses scanned QR strings, decodes RTMP URL and stream key, and populates secure input fields.
3. **Live Handshake & Ping Probe**:
   - Opens non-blocking TCP / UDP socket connection to target host and port.
   - Calculates ICMP/TCP handshake time (RTT in ms) and socket jitter.

---

### 11. Creator Profile & Connected Channels Hub

#### Functional Scope:
Multi-platform OAuth2 token management, Pro subscription billing, Cloud Vault storage sync policies.

#### Implementation Architecture & APIs:
1. **OAuth2 Channel Token Security**:
   - Android Jetpack `EncryptedSharedPreferences` / Android Keystore System for secure storage of YouTube and Twitch OAuth2 access and refresh tokens.
2. **Google Play Billing Integration**:
   - `BillingClient` 6.0+ implementation for Pro Studio subscription purchases, query product details, handle purchase token verification with backend validation.
3. **Cloud Vault Background Synchronization**:
   - Android `WorkManager` with `Constraints.Builder()` (`setRequiredNetworkType(NetworkType.UNMETERED)`, `setRequiresCharging(true)`).
   - Uploads completed VODs and clips to cloud storage with resumable multi-part upload workers.

---

### 12. Performance & Stream Diagnostics Analytics

#### Functional Scope:
Real-time telemetry collection, thermal throttling monitor, hardware auto-optimizer.

#### Implementation Architecture & APIs:
1. **System Telemetry Polling**:
   - **FPS Counter**: Calculates rendered vs. dropped frames per second in the OpenGL render thread.
   - **Bitrate Monitor**: Aggregates bytes transmitted through network sockets over 1-second intervals.
   - **SoC Thermals**: Monitors `HardwarePropertiesManager.getDeviceTemperatures()` or `PowerManager` thermal status.
   - **Battery Drain**: Evaluates microamperes drawn via `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW`.
2. **Real-Time Bezier Graph Rendering**:
   - Jetpack Compose Canvas / Custom View rendering smoothed cubic bezier paths (`Path.cubicTo()`) with telemetry history arrays.
3. **Auto-Optimize Encoder Algorithm**:
   - Dynamic Adaptive Bitrate (ABR): If socket queue buffer grows (indicating network congestion) or thermal status reaches `THROTTLING_LIGHT`, dynamically adjusts `MediaCodec` bitrate on the fly using:
     ```kotlin
     val params = Bundle().apply {
         putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newBitrate)
     }
     mediaCodec.setParameters(params)
     ```

---

### 13. Studio Vault & Media Library

#### Functional Scope:
Local and cloud media file management, metadata extraction, batch operations, video playback.

#### Implementation Architecture & APIs:
1. **MediaStore & File Storage Operations**:
   - Android `MediaStore.Video.Media` query for scoped storage compliance on Android 11+ (API 30+).
   - Generates thumbnails via `MediaMetadataRetriever` or `Coil` video frame decoder.
2. **Batch Processing Operations**:
   - Batch Deletion: Uses `MediaStore.createDeleteRequest()` for system permission prompt on Android 11+.
   - Batch Compression: Queues low-bitrate H.265 transcoding jobs using `WorkManager`.

---

### 14. Timeline Video Editor

#### Functional Scope:
Multi-track video and audio non-linear editing, split/cut, speed ramping, transitions, master export.

#### Implementation Architecture & APIs:
1. **Multi-Track Playhead Scrubber**:
   - Frame-accurate video decoding using `MediaCodec` and `ExoPlayer` / `Media3 Transformer`.
   - Audio waveform extraction: Pre-computes peak amplitude points from audio track and renders waveform bars.
2. **Editing Operations Engine**:
   - Split: Divides clip item into two distinct time-interval descriptors without duplicating source files on disk.
   - Speed Ramping: Adjusts playback presentation timestamps (PTS) by scaling factor (0.25x to 4.0x) using FFmpeg filter `setpts=PTS/SPEED` or Media3 `SpeedProvider`.
3. **Master Video Export**:
   - Jetpack Media3 `Transformer` hardware-accelerated pipeline exporting to MP4 (AVC/HEVC + AAC).

---

### 15. Floating Ball & Settings (System Permissions Hub)

#### Functional Scope:
System permission verification, overlay permission intent routing, hardware codec selection.

#### Implementation Architecture & APIs:
1. **Permission Orchestration Engine**:
   - Overlay: `Settings.canDrawOverlays(context)`. If false, launch `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
   - Audio Capture: `Manifest.permission.RECORD_AUDIO`.
   - Camera: `Manifest.permission.CAMERA`.
   - Notifications: `Manifest.permission.POST_NOTIFICATIONS` (Android 13+).
2. **Hardware Codec Picker**:
   - Scans device capabilities using `MediaCodecList(MediaCodecList.REGULAR_CODECS)` to detect hardware support for:
     - `video/avc` (H.264)
     - `video/hevc` (H.265)
     - `video/av01` (AV1 Hardware)

---

### 16. Facecam & Chroma Key Studio

#### Functional Scope:
Camera preview tuning, shape masking, GPU green screen keying, RGB rim lighting.

#### Implementation Architecture & APIs:
1. **OpenGL ES Custom Shaders**:
   - **Shape Mask Fragment Shader**: Discards fragments outside the defined geometric boundary (Circle radius, Squircle equation, or Hexagonal distance function).
   - **Chroma Key GPU Fragment Shader**:
     - Converts RGB texture to YUV / HSV color space.
     - Compares distance from user-selected key color (e.g., `#00FF00` green).
     - Computes alpha transparency based on threshold and smoothness tolerance.
2. **RGB Rim Glow Shader**:
   - Computes edge detection (Sobel filter) on the alpha mask outline and blends user-selected neon tint (`#FF2D55`, `#00F0FF`) with glow radius.

---

### 17. Broadcast Setup & RTMP Studio

#### Functional Scope:
Low-level encoder calibration, rate control configuration, scene layout management.

#### Implementation Architecture & APIs:
1. **MediaFormat Calibration**:
   - Configures `MediaFormat.KEY_BIT_RATE` (1,000 to 15,000 Kbps).
   - Configures `MediaFormat.KEY_BITRATE_MODE`:
     - `BITRATE_MODE_CBR` (Constant Bitrate for live streaming stability).
     - `BITRATE_MODE_VBR` (Variable Bitrate for local recording efficiency).
   - Configures `MediaFormat.KEY_I_FRAME_INTERVAL` (Keyframe interval: 2 seconds for RTMP standard compliance).

---

## 3. Engineering Implementation Roadmap & Phased Execution

```
Phase 1: Foundation & Recording Core (Weeks 1–3)
├── ScreenRecorderService & MediaProjection Handshake
├── Dual-Bus Audio Engine (AudioPlaybackCapture + Mic Oboe mixer)
├── Hardware MediaCodec Pipeline (AVC/HEVC)
└── Capture Hub & Basic Video Vault Integration

Phase 2: Floating Assistive Overlay & In-Game HUD (Weeks 4–5)
├── WindowManager Overlay Service (TYPE_APPLICATION_OVERLAY)
├── Touch Physics, Velocity Tracking & Magnetic Snap
├── 360° Radial Quick Menu & Canvas Telestrator
└── Rolling 30-Second AI Highlight Ring Buffer

Phase 3: Multistream Broadcast & Streaming Ingest (Weeks 6–8)
├── Parallel Socket Multiplexer (RTMP / SRT)
├── Pre-Flight Diagnostic Validation & DND Shield
├── CameraX PIP Preview & OpenGL ES Chroma Key Shader
└── Live Multistream Studio & Custom Ingest Node Modal

Phase 4: Audio DSP, Overlays & Real-Time Moderation (Weeks 9–10)
├── 4-Channel DSP Audio Mixer & DMCA Dual-Track VOD Shield
├── WebSocket Chat Aggregation (YouTube, Twitch, Kick) & Moderation Drawer
└── Alert & Widget Studio (Dynamic Goals, Follower Chimes, Super Chats)

Phase 5: Post-Production & Creator Tools (Weeks 11–12)
├── Instant 9:16 Shorts Clip Trimmer & Social Sharing
├── Media3 Timeline Video Editor
└── Performance Telemetry Monitor & Adaptive Bitrate Auto-Optimizer
```

---

## 4. Key Android Permissions Manifest Reference

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Screen Capture & Floating Overlay -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    
    <!-- Audio & Camera -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- System Controls & DND Shield -->
    <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <!-- Network & Hardware Diagnostics -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <!-- Storage & Billing -->
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="com.android.vending.BILLING" />
</manifest>
```

This technical specification provides the exact native Android APIs, background architecture, and mathematical algorithms necessary to implement every feature designed for **PulseCast Studio**.

# PulseCast Studio — Spec vs. Implementation Review

Comparison of [`pulsecast_studio_implementation_plan.md`](file:///a:/projects/Java_projects/PulseCast/stitch_hybrid_android_screen_studio/pulsecast_studio_implementation_plan.md) and [`pulsecast_studio_navigation_interaction_specification.md`](file:///a:/projects/Java_projects/PulseCast/stitch_hybrid_android_screen_studio/pulsecast_studio_navigation_interaction_specification.md) against the current codebase.

---

## Executive Summary

The project has a **solid and correctly-wired Phase 1–2 foundation** — navigation shell, recording pipeline, and overlay services are in production-quality shape. The UI layer is more mature than the average prototype (most screens have real Compose implementations, not just stubs). However, the **business-logic backend for every feature beyond basic recording is absent or mocked**, several screens remain as `DestinationPlaceholder`, and a set of spec-required interactions are missing or partially implemented.

---

## ✅ What Is Fully Implemented

### Navigation Shell (Spec §1 Global App Bar + §2 Bottom Nav)
- [`PulseCastNavigation.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/navigation/PulseCastNavigation.kt) correctly wires all 17 routes defined in the spec.
- **App Bar** — Logo → CaptureHub, Storage pill → Vault, 📊 → Diagnostics, Avatar → CreatorHub: **all connected**.
- **Bottom Nav** — Record / Live / Editor / Tools tabs plus the center Quick Orb FAB: **all connected**.
  - The Quick Orb FAB opens `FloatingBallCustomizationScreen` as a full-screen overlay (no nav pop-up), which matches the spec's "overlay" intent.

### Recording Core (Spec §1 — Capture Hub)
| Spec Requirement | Implementation |
|---|---|
| `MediaProjectionManager` + `ActivityResultLauncher` handshake | ✅ [`MainActivity.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/MainActivity.kt) L35–38 |
| 3-second countdown overlay | ✅ `CountdownOverlay` composable, `rememberSaveable` countdown state |
| `moveTaskToBack` / app minimises | ✅ Activity triggers service; HUD overlay service covers screen |
| `ScreenRecordService` foreground with `mediaProjection` type | ✅ [`ScreenRecordService.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/service/ScreenRecordService.kt) |
| `VirtualDisplay` + `MediaCodec` `VideoEncoder` | ✅ [`VideoEncoder.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/encoder/VideoEncoder.kt), `AudioEncoder.kt` |
| Resolution scaling + bitrate calculation | ✅ [`RecordingConfig.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/core/RecordingConfig.kt) `BitrateCalculator` + `ResolutionScaler` |
| Pause / Resume / Stop state machine | ✅ [`RecordingStateMachine.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/core/RecordingStateMachine.kt), `PtsAdjuster.kt` |
| `MediaStore` scoped-storage save | ✅ [`SaveLocation.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/core/SaveLocation.kt) |
| Storage telemetry → `freeSpaceText` in UI | ✅ `CaptureViewModel` exposes `storagePercentage`, `freeSpaceText` |

### Live In-Game HUD Overlay Service (Spec §4)
| Spec Requirement | Implementation |
|---|---|
| `TYPE_APPLICATION_OVERLAY` + `FLAG_NOT_FOCUSABLE` | ✅ [`LiveHUDOverlayService.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/service/LiveHUDOverlayService.kt) L68–81 |
| Floating orb single-tap → radial menu fan-out | ✅ L167, radial `isRadialOpen` state toggle |
| Annotation / brush toolbar slide-in | ✅ `AnimatedVisibility` + `BrushToolbarHUD` with Bezier/shape tools |
| Chat drawer slide from edge | ✅ `AnimatedVisibility` + `slideInHorizontally` + `LiveChatDrawer` |
| REC timer + telemetry pill | ✅ `CompactTelemetryPill` blinking dot, monospace timer |
| Draggable Facecam PIP | ✅ `LivePIP` with `detectDragGestures` + `IntOffset` |
| Stop radial slot → sends `ACTION_STOP` | ✅ `HUDActionCircle("Stop")` → `sendServiceAction(ACTION_STOP)` |
| Pause radial slot | ✅ `HUDActionCircle("Pause")` → `ACTION_PAUSE` |

### Streaming Pipeline Scaffold (Spec §2 — Multistream)
| Spec Requirement | Implementation |
|---|---|
| Fan-out single encoder → multiple sinks | ✅ [`FanOutDistributor.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/streaming/FanOutDistributor.kt), `RtmpDestinationSink` |
| RTMP publisher (root-encoder) | ✅ [`RootEncoderRtmpPublisher.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/streaming/RootEncoderRtmpPublisher.kt) |
| Reconnect backoff per destination | ✅ [`ReconnectBackoff.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/streaming/ReconnectBackoff.kt) |
| NAL unit parsing | ✅ [`NalUnitParser.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/streaming/NalUnitParser.kt) |

### UI Screens with Full Compose Implementations
These screens exist as **real, detailed Compose implementations** (not placeholders):

| Screen | File | Completeness |
|---|---|---|
| Capture Hub | [`CaptureHUD.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/capture_hub/CaptureHUD.kt) (766 lines) | High — telemetry card, record button, presets grid, vault preview |
| Live Multistream Studio | [`LiveMultiStreamStudio.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/live_multistream_studio/LiveMultiStreamStudio.kt) (697 lines) | High — platform toggles, viewfinder, audio dock, bottom action dock |
| Pre-Stream Checklist | [`PreStreamGoLiveSafetyChecklist.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/pre_stream_go_live_safety_checklist/PreStreamGoLiveSafetyChecklist.kt) (27KB) | High |
| Floating Ball Customization | [`FloatingBallCustomizationGestureBinder.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/floating_ball_customization_gesture_binder/FloatingBallCustomizationGestureBinder.kt) (39KB) | High |
| Studio Vault & Media Library | [`StudioVaultMediaLibrary.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/studio_vault_media_library/StudioVaultMediaLibrary.kt) (25KB) | High |
| Live Chat Drawer | [`LiveStreamChatUnifiedModerationDrawer.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/live_stream_chat_unified_moderation_drawer/LiveStreamChatUnifiedModerationDrawer.kt) (24KB) | High |
| Floating Ball & Settings | [`FloatingBall_and_Settings.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/floating_ball_and_settings/FloatingBall_and_Settings.kt) (20KB) | High |
| In-Game HUD (Compose screen) | [`LiveInGameHUD_Overlay.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/live_in_game_hud_overlay/LiveInGameHUD_Overlay.kt) (18KB) | High |

---

## ⚠️ Partially Implemented (UI Exists, Business Logic Missing or Mocked)

### 1. Audio Engine (Spec §1 AudioPlaybackCapture + Oboe Mixer)
- **Current**: `AudioEncoder.kt` captures mic via `AudioRecord`. The UI in `CaptureHUD.kt` shows "Internal + Mic" toggle button that calls `{}` (no-op).
- **Missing**:
  - `AudioPlaybackCaptureConfiguration` for internal system audio loopback (requires `MediaProjection` reference inside `AudioEncoder`).
  - Oboe/AAudio dual-bus mixer thread for mixing internal audio + mic PCM.
  - Audio source bus selection (Internal Only / Mic Only / Mixed) is **not wired** — the toggle in `TelemetryReadinessCard` does nothing.
  - `FOREGROUND_SERVICE_MICROPHONE` permission is not in `AndroidManifest.xml` (spec §4 requires it).

### 2. Resolution / FPS Profile Selector (Spec §1 — Capture Hub, Spec A)
- **Current**: `ScreenRecordService` hardcodes `Quality.MEDIUM` (1080p) and `frameRate = 30`. `RecordingConfig` has `Quality.HIGH` (1440p) and `Quality.ORIGINAL` but they're unused.
- **UI**: `CaptureHUD.kt` renders `TargetPresetsSection` with resolution / FPS chips (`1080p`, `2K`, `4K`, `60 FPS`, `120 FPS`).
- **Missing**: The selected chip value is **not passed** through `CaptureViewModel` → `ScreenRecordService`. Resolution and FPS selection is purely cosmetic. The spec requires `MediaCodecList` validation and `Display.getSupportedModes()` for 120 FPS.

### 3. GO LIVE → Pre-Stream Checklist → HUD Flow (Spec B/C)
- **Current**: `onGoLive` in navigation bypasses the checklist and navigates directly to `LiveHud` route. The checklist is rendered *inside* `LiveMultiStreamStudioScreen` as a local `Box` overlay.
- **Issue**: The `SafetyChecklist` route is registered in the nav graph (`PulseCastRoute.SafetyChecklist`) but `LiveStudioDestination` in [`PulseCastNavigation.kt`](file:///a:/projects/Java_projects/PulseCast/app/src/main/java/com/masterminds/pulsecast/ui/navigation/PulseCastNavigation.kt) L148 passes `onGoLive = { navController.navigate(PulseCastRoute.LiveHud) }` — it skips the checklist nav route entirely. The checklist is shown as a pop-up inside the studio screen instead.
- **Missing business logic in checklist**:
  - Battery level polling (`BatteryManager.EXTRA_LEVEL`) — UI shows static "94%" value.
  - Thermal status polling (`PowerManager.thermalStatus`) — UI shows static "38.5°C".
  - Network uplink probe — static "Good" badge.
  - Mic RMS dB metering — static meter bar.
  - DND Shield actual `NotificationManager` invocation — button exists but calls `{}`.
  - Private rehearsal stream mode (YouTube unlisted / Twitch test) — button exists, no-op.

### 4. RTMP / SRT Actual Connection (Spec §2, §10)
- **Current**: `RootEncoderRtmpPublisher.kt` exists and wraps the library, but a comment in `ScreenRecordService.kt` explicitly states: *"RootEncoderRtmpPublisher isn't fully wired to the real library yet"*. Destinations are created per stream URL but actual RTMP handshake success is not verified.
- **Missing**:
  - SRT (`libsrt` JNI binding) is entirely absent — no `libsrt.so`, no JNI bridge, no SRT socket config (`SRTO_LATENCY`, `SRTO_PASSPHRASE`).
  - Custom Ingest UI (`CustomIngestDestination`) has a "Test Handshake & Ping" button wired to `{}` (no-op). No socket probe, no RTT measurement.
  - QR code scanner (ML Kit `BarcodeScanning`) for stream key is absent.
  - Live follower count polling (YouTube Data API, Twitch Helix) — destination cards show static follower counts.

### 5. Custom RTMP Ingest → Live Studio Feedback Loop (Spec J → B)
- **Current**: `CustomIngestDestination` calls `onConnect()` which just `popBackStack()`. No `StreamDestination` is created or persisted to any state holder that `LiveMultiStreamStudioScreen` can observe.
- **Missing**: A `ViewModel` or `StateFlow` that holds the list of active custom destinations and pushes them into `ScreenRecordService` at go-live time.

### 6. Audio Mixer (Spec §8)
- **Current**: `AudioMixerDestination` in `PulseCastNavigation.kt` renders 4 channel strips (Game, Mic, Discord, Music) with `LinearProgressIndicator` and a static `Slider(.72f, {})` (non-interactive master fader).
- **Missing**:
  - All sliders are non-interactive (lambdas `{}`). No `ViewModel` state backing them.
  - Smart ducking compressor: side-chain compressor logic absent.
  - DMCA dual-bus routing: only a `Switch(true, {})` checkbox — no actual separate AAC bus creation.
  - SFX quick deck pads: not present in this implementation (spec §H requires 6 pads).

### 7. Floating Ball Customization → Service Sync (Spec §6 / F)
- **Current**: `FloatingBallCustomizationGestureBinder.kt` is 39KB — likely has the full UI (gesture bindings, opacity sliders, orb skins). However, gesture changes are not persisted or broadcast to `LiveHUDOverlayService`.
- **Missing**:
  - `DataStore` persistence of gesture bindings (spec: Jetpack DataStore Proto/Preferences).
  - `LocalBroadcastManager` / `SharedFlow` bridge to update the running `LiveHUDOverlayService` without restarting it.
  - Drag physics simulator in settings uses Compose animations, but the actual `DynamicAnimation.SpringAnimation` for the overlay ball uses simple `detectDragGestures` with no magnetic snap to edge (spec requires `VelocityTracker` + spring physics snap).

### 8. Floating Orb Physics (Spec §4 — Touch Interceptor)
- **Current**: The orb in `LiveHUDOverlayService` is fixed to `Alignment.CenterEnd` with `.padding(end = 12.dp)`. The `LiveInGameHUD_Overlay.kt` Compose screen may have drag logic.
- **Missing**: 
  - Spec requires the orb to be **freely draggable** anywhere on screen (not fixed to right edge), with magnetic edge snapping via `SpringAnimation`.
  - Double-tap for AI clip buffer → no `Handler.postDelayed` double-tap detection, no ring buffer, no `MediaMuxer` circular queue.
  - Long-press mic mute → not implemented in the service's orb touch handler.
  - Swipe inward to open chat → the chat exists but is toggled via a radial menu slot, not a swipe gesture.

---

## ❌ Not Yet Implemented (Placeholder or Missing Entirely)

| Screen / Feature | Spec Section | Status |
|---|---|---|
| **Timeline Video Editor** | §14, Spec N | `DestinationPlaceholder("Timeline Video Editor", "SCREEN_29")` — empty shell |
| **Performance & Stream Diagnostics** | §12, Spec L | `DestinationPlaceholder("Performance & Stream Diagnostics", "SCREEN_17")` — empty shell |
| **Creator Profile & Connected Channels** | §11, Spec K | `DestinationPlaceholder("Creator Profile & Connected Channels", "SCREEN_15")` — empty shell |
| **Instant Clip & Highlight Export** | §9, Spec I | Route exists (`ClipExport`) → `DestinationPlaceholder("Instant Clip & Highlight Export", "SCREEN_9")` |
| AI highlight detection / event markers | §9.1 | Absent |
| Smart 9:16 reframing pipeline | §9.2 | Absent |
| `MediaExtractor` + `MediaMuxer` fast trim | §9.3 | Absent |
| Android Share Dispatcher (`FileProvider`) | §9.4 | Absent |
| **OpenGL ES / GPU Rendering Pipeline** | §7, §16 | Absent — no `EGLSurface`, no GLSL shaders |
| Chroma key GPU fragment shader | §16.1 | `FacecamDestination` has a `Switch` toggle, no actual shader |
| Shape mask (circle/squircle/hexagon) | §16.1 | Shape chips exist in UI, no GLSL mask |
| RGB rim glow Sobel edge shader | §16.2 | No implementation |
| Alert widget OpenGL FBO composition | §7.1 | No implementation |
| **WebSocket chat ingestion** | §5.1 | UI renders hardcoded sample chat messages; no WebSocket client |
| Twitch IRC WebSocket | §5.1 | Absent |
| YouTube Live Chat polling | §5.1 | Absent |
| Kick Pusher WebSocket | §5.1 | Absent |
| Chat velocity calculator | §5.2 | Absent |
| Moderation actions (ban, timeout) | §5.3 | Buttons exist in UI, `{}` no-op |
| **AI 30s replay ring buffer** | §4.4 | Absent — no circular `MediaMuxer` queue |
| SFX soundboard (`SoundPool`) | §7.3 | Absent |
| **Google Play Billing** | §11.2 | Absent |
| OAuth2 token management (EncryptedSharedPrefs) | §11.1 | Absent |
| WorkManager cloud sync | §11.3 | Absent |
| `MediaCodec` adaptive bitrate adjustment | §12.3 | Absent |
| `HardwarePropertiesManager` SoC thermals | §12.1 | Absent |
| `FPS` / `Bitrate` live telemetry collection | §12.1 | Absent |
| Real-time Bezier graph rendering | §12.2 | Screen is a placeholder |
| `MediaStore.Video.Media` query + thumbnails | §13.1 | UI shows hardcoded thumbnail cards |
| Batch `MediaStore.createDeleteRequest()` | §13.2 | Absent |

---

## Manifest Gap vs. Spec §4 (Permissions)

| Permission (Spec Requirement) | AndroidManifest.xml | Status |
|---|---|---|
| `FOREGROUND_SERVICE` | ✅ Present | ✅ |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | ✅ Present | ✅ |
| `FOREGROUND_SERVICE_CAMERA` | ✅ Present | ✅ |
| **`FOREGROUND_SERVICE_MICROPHONE`** | ❌ Missing | ❌ Required for Oboe mic |
| `SYSTEM_ALERT_WINDOW` | ✅ Present | ✅ |
| `RECORD_AUDIO` | ✅ Present | ✅ |
| `CAMERA` | ✅ Present | ✅ |
| `POST_NOTIFICATIONS` | ✅ Present | ✅ |
| **`ACCESS_NOTIFICATION_POLICY`** | ❌ Missing | ❌ Required for DND Shield |
| `INTERNET` | ✅ Present | ✅ |
| `ACCESS_NETWORK_STATE` | ✅ Present | ✅ |
| **`ACCESS_WIFI_STATE`** | ❌ Missing | ❌ Spec requires for uplink probe |
| **`WAKE_LOCK`** | ❌ Missing | ❌ Required for long-running stream |
| **`READ_MEDIA_VIDEO`** | ❌ Missing | ❌ Required for Vault thumbnails |
| **`READ_MEDIA_IMAGES`** | ❌ Missing | ❌ Required for Screenshots tab |
| **`com.android.vending.BILLING`** | ❌ Missing | ❌ Required for Play Billing |

---

## Navigation Interaction Gaps vs. Spec

| Spec Interaction | Expected Behaviour | Current Behaviour |
|---|---|---|
| **Record button long-press on Audio toggle** | Opens Audio Mixer | No long-press handler |
| **Capture Hub → "View All Vault" link** | Navigate to Vault | ✅ Connected via `onNavigateToVault` |
| **GO LIVE → Safety Checklist route** | Full-screen route | Inline overlay inside LiveStudio |
| **Pre-Checklist "Test Rehearsal"** | Private unlisted stream | No-op |
| **Pre-Checklist "Enable DND Shield"** | `NotificationManager` DND | No-op |
| **Orb Double-Tap → AI Clip** | Saves 30s ring buffer | Not implemented |
| **Orb Long-Press → Mic Mute** | Hardware mic mute | Not implemented |
| **Orb Swipe Inward → Chat Drawer** | Swipe gesture | Chat opened via radial slot instead |
| **Orb Drag + Edge Snap** | Spring physics snap | Fixed to right edge, no physics |
| **Radial Slot 5: Soundboard** | SFX mini deck | Not in current radial (only Pause/Brush/Cam/Stop) |
| **Chat "Approve" / "Ban" inline actions** | REST API mutations | No-op buttons |
| **Audio Mixer sliders** | Actual dB gain adjustment | All static / no ViewModel |
| **Creator Hub "Add Ingest"** | Opens Custom Ingest modal | Screen is a placeholder |
| **Vault "Edit" icon** | Opens Timeline Editor | Screen is a placeholder |
| **Diagnostics "Auto-Optimize"** | Dynamic `MediaCodec` bitrate | Screen is a placeholder |

---

## Phase Roadmap Completion Status (vs. Spec §3)

```
Phase 1: Foundation & Recording Core           ████████░░  ~80% complete
  ✅ ScreenRecorderService & MediaProjection
  ✅ Hardware MediaCodec Pipeline (AVC)
  ✅ Capture Hub UI
  ❌ Dual-Bus Audio Engine (Oboe mixer missing)
  ⚠️  Quality/FPS profile not wired to service

Phase 2: Floating Overlay & In-Game HUD        ███████░░░  ~65% complete
  ✅ WindowManager Overlay Service
  ✅ Radial Quick Menu (4 of 5 slots)
  ✅ Canvas Telestrator (brush toolbar)
  ❌ VelocityTracker + Spring Edge Snap
  ❌ 30-Second AI Highlight Ring Buffer
  ❌ Double-tap / Long-press gestures

Phase 3: Multistream & Streaming Ingest        █████░░░░░  ~45% complete
  ✅ Fan-out RTMP architecture scaffolding
  ✅ LiveMultiStreamStudio UI
  ✅ Pre-Flight Checklist UI
  ⚠️  RTMP handshake not fully wired
  ❌ SRT (libsrt) entirely absent
  ❌ Pre-flight live diagnostics (all static)
  ❌ CameraX PIP with OpenGL Chroma Key

Phase 4: Audio DSP, Overlays & Moderation      ██░░░░░░░░  ~20% complete
  ⚠️  Audio Mixer UI exists (non-functional)
  ⚠️  Chat Drawer UI exists (hardcoded data)
  ❌ Oboe 4-channel DSP implementation
  ❌ DMCA dual AAC bus recording
  ❌ WebSocket chat ingestion (all 3 platforms)
  ❌ Alert/Widget OpenGL composition

Phase 5: Post-Production & Creator Tools       █░░░░░░░░░  ~10% complete
  ❌ Clip Export screen (placeholder)
  ❌ Media3 Timeline Editor (placeholder)
  ❌ Performance Analytics (placeholder)
  ❌ AI event markers / ring buffer
```

---

## Priority Recommendations

### 🔴 Critical (Unblock Core Features)
1. **Wire resolution/FPS UI selection to `ScreenRecordService`** — current recording is always 1080p/30fps regardless of what user selects.
2. **Add `FOREGROUND_SERVICE_MICROPHONE` + `AudioPlaybackCapture`** — internal system audio loopback is the primary differentiator from a basic recorder.
3. **Fix floating orb gestures** — double-tap and long-press are in the spec interaction table but absent from the service's touch handler.
4. **Connect `CustomIngestDestination` to a shared ViewModel** so custom RTMP endpoints persist to `LiveMultiStreamStudio` and are passed to `ScreenRecordService` at go-live.

### 🟡 High Priority (Phase 3 Completion)
5. **Pre-flight diagnostics** — replace static values with real `BatteryManager`, `PowerManager.thermalStatus`, and mic RMS polling.
6. **DND Shield `NotificationManager` integration** — the permission check + invocation is a single API call.
7. **Add missing manifest permissions** — `WAKE_LOCK`, `ACCESS_WIFI_STATE`, `ACCESS_NOTIFICATION_POLICY`, `READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`.

### 🟢 Medium Priority (Phase 4–5)
8. **WebSocket chat** — a minimal Twitch IRC WebSocket client would light up the chat drawer with real data.
9. **`MediaCodec` DMCA dual-bus recording** — second `AudioEncoder` instance writing a separate track to the `MuxerWrapper`.
10. **`MediaExtractor` + `MediaMuxer` fast trim** for the Clip Export flow before building the full Media3 editor.

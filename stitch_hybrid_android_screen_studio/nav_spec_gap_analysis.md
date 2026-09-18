# PulseCast Studio — Navigation Spec Review & Gap Analysis

> Source: `pulsecast_studio_complete_interaction_navigation_specification.md`

---

## Architecture Overview (from spec)

The spec defines **5 bottom nav anchors**, **17 named screens**, and a clear navigation hierarchy. Below is the full gap analysis against the current implementation.

---

## Bottom Navigation — Correction Required

> [!IMPORTANT]
> The spec (§2, row 3) explicitly defines the **Center FAB** as the **Floating Orb** that navigates to `Floating Ball Customization & Gesture Binder (SCREEN_8)` — **not** a record button. The big crimson `● REC` button lives **inside** the Capture Hub screen (§3A). The current implementation uses the center FAB as a record toggle — this contradicts the spec.

| Slot | Spec Says | Currently Implemented | Status |
|---|---|---|---|
| Tab 1: Record | `capture_hub` (SCREEN_28) | ✅ `CaptureHubScreen` | ✅ Correct |
| Tab 2: Live | `live_multistream_studio` (SCREEN_32) | ✅ `LiveMultistreamStudioScreen` | ✅ Correct |
| **Center FAB** | Floating Orb → opens `floating_ball_customization_gesture_binder` (SCREEN_8) | ❌ Currently fires record start/stop | ❌ Wrong |
| Tab 3: Editor | `timeline_video_editor` (SCREEN_29) | ✅ `TimelineVideoEditorScreen` | ✅ Correct |
| Tab 4: Tools | `floating_ball_settings` (SCREEN_31) | ✅ `FloatingBallSettingsScreen` | ✅ Correct |

---

## Screen Inventory — 17 Screens Total

### ✅ Implemented (7 screens)

| Screen ID | Screen Name | File |
|---|---|---|
| SCREEN_28 | Capture Hub | [`CaptureHub.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/CaptureHub.kt) |
| SCREEN_32 | Live Multistream Studio | [`LiveMultistreamStudioScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/broadcast/LiveMultistreamStudioScreen.kt) |
| SCREEN_29 | Timeline Video Editor | [`TimelineVideoEditorScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/editor/TimelineVideoEditorScreen.kt) |
| SCREEN_31 | Floating Ball & System Settings | [`FloatingBallSettingsScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/tools/FloatingBallSettingsScreen.kt) |
| SCREEN_11 | Pro Multi-Track Audio Mixer & DMCA Shield | [`AudioMixerScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/audio/AudioMixerScreen.kt) |
| SCREEN_2 | Stream Alert & Widget Overlay Studio | [`OverlayStudioScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/overlays/OverlayStudioScreen.kt) |
| SCREEN_24 | Studio Vault & Media Library | [`MediaVaultScreen.kt`](file:///a:/projects/screen-recorder/app/src/main/kotlin/com/kwabena/screenrecorder/ui/vault/MediaVaultScreen.kt) |

### ❌ Missing (10 screens to build)

| Screen ID | Screen Name | Entry Point / Trigger |
|---|---|---|
| **SCREEN_8** | Floating Ball Customization & Gesture Binder | Center FAB tap → opens this as full screen |
| **SCREEN_18** | Live In-Game HUD & Overlay | `● REC` tap from Capture Hub → 3-2-1 countdown → this |
| **SCREEN_4** | Pre-Stream Go-Live Safety Checklist Modal | `GO LIVE` CTA in Live Studio |
| **SCREEN_6** | Stream Live Chat & Unified Moderation Drawer | Orb swipe-inward during SCREEN_18 |
| **SCREEN_9** | Instant Clip & Highlight Export Flow | Stop recording from SCREEN_18, or Editor Shorts button |
| **SCREEN_13** | Custom RTMP & SRT Ingest Node Modal | `+ Add Custom RTMP/SRT` in Live Studio |
| **SCREEN_19** | Facecam & Chroma Key Studio | Tap Camera PIP preview in Live Studio |
| **SCREEN_15** | Creator Profile & Connected Channels Hub | Profile avatar tap in App Bar |
| **SCREEN_17** | Performance & Stream Diagnostics Analytics | Diagnostics bar-graph icon in App Bar |
| **SCREEN_22** | Broadcast Setup & RTMP Studio | Encoder gear icon in Live Studio |

---

## Key Interaction Corrections Needed

### 1. Center FAB — Must become Floating Orb (SCREEN_8 launcher)
**Spec §2:** `"Center FAB: Single Tap → scale bounce (scale-110), haptic thud → Triggers Floating Ball Customization & Gesture Binder (SCREEN_8)"`

**Currently:** Center button triggers `startRecordingService`/`stopRecordingService` — this belongs on the `● REC` button *inside* Capture Hub.

### 2. `● REC` Button Inside Capture Hub (SCREEN_28)
**Spec §3A:** `"Big Crimson Record FAB → Single Tap → 3-second radial countdown overlay → minimizes app → launches Live In-Game HUD (SCREEN_18)"`

The big record button must:
1. Show `3... 2... 1... GO!` countdown overlay
2. Minimize/collapse the app
3. Launch the in-game transparent HUD overlay service (SCREEN_18)

### 3. App Bar Tappable Targets (Persistent, All Screens)
**Spec §1** defines 4 interactive targets currently stubbed:

| Target | Action |
|---|---|
| Brand Logo | → Capture Hub (root) |
| Storage badge | → Studio Vault (Cloud Vault tab) |
| Diagnostics icon | → Performance Analytics (SCREEN_17) |
| Profile avatar | → Creator Profile Hub (SCREEN_15) |

### 4. `FloatingBallCustomizationScreen.kt` — Still needs to exist
**Spec §3F, SCREEN_8** is the full Gesture Binder screen opened by the Center FAB. The recently corrected `FloatingBallSettingsScreen` is SCREEN_31 (Tools tab — system settings). SCREEN_8 is a separate, dedicated customization modal.

---

## Navigation Flow Hierarchy (from spec §4)

```
PulseCast Studio
│
├── App Bar (Global)
│   ├── [📊 Diagnostics] ──► SCREEN_17: Performance Analytics       ❌ Missing
│   ├── [👤 Avatar]      ──► SCREEN_15: Creator Profile Hub          ❌ Missing
│   └── [Storage Badge]  ──► SCREEN_24: Studio Vault                 ✅ Exists
│
├── Bottom Nav
│   ├── [Record]  ──► SCREEN_28: Capture Hub                        ✅ Exists
│   │                  └── [● REC] ──► SCREEN_18: In-Game HUD        ❌ Missing
│   │
│   ├── [Live]    ──► SCREEN_32: Live Multistream Studio             ✅ Exists
│   │                  ├── [+ RTMP]     ──► SCREEN_13: RTMP Modal     ❌ Missing (as proper modal)
│   │                  ├── [🎨 Overlays]──► SCREEN_2:  Overlay Studio ✅ Exists (needs nav wiring)
│   │                  ├── [🎚️ Mixer]   ──► SCREEN_11: Audio Mixer    ✅ Exists (needs nav wiring)
│   │                  ├── [Facecam PIP]──► SCREEN_19: Chroma Key     ❌ Missing (as proper screen)
│   │                  └── [GO LIVE]    ──► SCREEN_4:  Safety Check   ❌ Missing (as proper modal)
│   │                                        └── [Launch] ──► SCREEN_18 ❌
│   │
│   ├── [Center Orb FAB] ──► SCREEN_8: Gesture Binder               ❌ Wrong (currently fires record)
│   │
│   ├── [Editor]  ──► SCREEN_29: Timeline Video Editor               ✅ Exists
│   │                  └── [Shorts/AI] ──► SCREEN_9: Clip Export     ❌ Missing (as proper screen)
│   │
│   └── [Tools]   ──► SCREEN_31: Floating Ball & System Settings     ✅ Exists
│                      └── ["Customize Floating Ball"] ──► SCREEN_8  ❌ Missing
│
└── SCREEN_18: Live In-Game HUD (active during recording)
    ├── [Swipe inward] ──► SCREEN_6: Live Chat Drawer                ❌ Missing
    ├── [Radial Stop]  ──► SCREEN_9: Clip Export                    ❌ Missing
    └── [Radial Mixer] ──► SCREEN_11: Audio Mixer                   ✅ Exists (needs nav)
```

---

## Engineering Implementation Guidelines (from spec §5)

### Haptic Feedback Conventions
| Intensity | Duration | When |
|---|---|---|
| Light tick | 5ms | Chips, sliders, tab nav, macro pills |
| Medium click | 12ms | Button taps, toggle switches, radial fan-outs |
| Heavy thud | 30ms | Record start/stop, DND shield arm, go-live launch |

### Animation Specs
| Element | Curve / Spring | Duration |
|---|---|---|
| Drawers & Bottom Sheets | `cubic-bezier(0.16, 1, 0.3, 1)` | 280ms |
| Floating Orb Snap | `spring(damping: 24, stiffness: 280)` | Physics |
| Modals & Overlays | Backdrop fade `ease-out`, card `scale(0.95→1.0)` | 200ms |

### Design Tokens (canonical from spec)
| Token | Value |
|---|---|
| Base Surface | `#0E131E` |
| Elevated Cards | `#171B27` |
| Primary Accent / Live | `#FF2D55` |
| Telemetry / Diagnostic Cyan | `#00F0FF` |
| Warnings / Super Chats | `#FFD600` / `#F59E0B` |

---

## Recommended Build Order

1. **Fix Center FAB** → wire to `FloatingBallCustomizationScreen` (SCREEN_8, gesture binder), not record
2. **SCREEN_8** — Floating Ball Customization & Gesture Binder (full dedicated screen)
3. **SCREEN_18** — Live In-Game HUD Overlay (system window service + Compose overlay)
4. **SCREEN_4** — Pre-Stream Safety Checklist (bottom sheet modal in Live tab)
5. **SCREEN_6** — Live Chat Moderation Drawer (sliding drawer from HUD)
6. **SCREEN_9** — Instant Clip & Highlight Export (sheet in Editor + HUD)
7. **SCREEN_19** — Facecam & Chroma Key Studio (full screen from Live tab PIP tap)
8. **SCREEN_13** — Custom RTMP/SRT Modal (already partially exists, needs full spec)
9. **SCREEN_17** — Performance & Diagnostics Analytics (app bar diagnostics icon)
10. **SCREEN_15** — Creator Profile & Connected Channels Hub (app bar avatar tap)
11. **SCREEN_22** — Broadcast Setup & RTMP Studio (encoder gear from Live tab)

# PulseCast Studio — Interaction Specification & Navigation Flow Architecture (.MD)

This document provides a comprehensive, engineering-ready specification of all **button clicks, interactive touch targets, micro-interactions, gestures, state changes, and target destinations** across the **PulseCast Studio** ecosystem (blending XRecorder & Prism Live Studio).

---

## 1. Global App Bar Header (Persistent Across Primary Screens)

Present on: `Capture Hub`, `Live Multistream Studio`, `Timeline Video Editor`, `Floating Ball & Settings`, `Studio Vault`, `Broadcast Setup`, `Performance Analytics`, `Creator Hub`.

| UI Element / Tap Target | Interaction Trigger | Micro-Interaction & Visual Feedback | Target Destination / Action |
| :--- | :--- | :--- | :--- |
| **Brand Logo & Wordmark** (`PulseCast Pro`) | Single Tap | Subtle scale-down (`active:scale-95`), haptic tick | Navigates to **Capture Hub** (Home root). |
| **Storage Indicator Badge** (`128 GB Free • ~18h 45m`) | Single Tap | Glows subtle cyan, expands quick storage breakdown tooltip | Navigates directly to **Studio Vault & Media Library** (`Cloud Vault` tab). |
| **Diagnostics Bar Graph Icon** (`📊`) | Single Tap | Haptic click, icon highlights in `#00f0ff` Cyan | Opens **Performance & Stream Diagnostics Analytics** screen. |
| **Creator Profile Avatar** (`👤` / Gamer Headshot) | Single Tap | Profile ring pulses `#ff2d55`, haptic feedback | Opens **Creator Profile & Connected Channels Hub** screen. |
| **Back Button Arrow** (`←` on secondary screens) | Single Tap | Slide transition reverse, haptic tick | Pops current view back to previous screen or primary hub. |

---

## 2. Global Bottom Navigation Bar (5 Primary Anchor Targets)

| Tab Item | Icon & Badge | Interaction Trigger | Micro-Interaction | Screen / Destination |
| :--- | :--- | :--- | :--- | :--- |
| **Tab 1: Record** | Video Camcorder icon | Single Tap | Icon tints active Crimson, subtle bounce animation | Navigates to **Capture Hub** (`SCREEN_28`). |
| **Tab 2: Live** | Broadcast Antenna / Signal wave | Single Tap | Red active pip appears below, glows Crimson | Navigates to **Live Multistream Studio** (`SCREEN_32`). |
| **Center FAB: Quick Orb** | Floating Assistive Orb (`Pulse Logo`) | Single Tap | Scale bounce (`scale-110`), haptic thud | Triggers **Floating Ball Customization & Gesture Binder** (`SCREEN_8`) or toggles system-wide overlay. |
| **Tab 3: Editor** | Filmstrip Clapper / Scissor | Single Tap | Scissor snips animation, tints Crimson | Navigates to **Timeline Video Editor** (`SCREEN_29`). |
| **Tab 4: Tools** | Sliders / DSP Equalizer icon | Single Tap | Equalizer bars bounce, tints Crimson | Navigates to **Floating Ball & System Settings** (`SCREEN_31`) or Studio Tools sheet. |

---

## 3. Screen-by-Screen Interaction & Navigation Map

---

### A. Capture Hub (Home Screen - `SCREEN_28`)

*Core recording hub for high-FPS mobile gaming and screen capture.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Big Crimson Record FAB** (`● REC`) | Single Tap | 3-second radial countdown overlay (`3... 2... 1... GO!`), screen collapses | Minimizes app and launches **Live In-Game HUD & Overlay** (`SCREEN_18`). |
| **Resolution Preset Pill** (`1080p` / `2K` / `4K`) | Single Tap | Active outline shifts, haptic bump | Updates active capture profile instantaneously. |
| **FPS Selector Pill** (`60 FPS` / `120 FPS`) | Single Tap | Smooth background slide between options | Toggles high-refresh rate screen capture buffer. |
| **Audio Source Switcher** (`Internal` / `Mic` / `Mute`) | Single Tap | Toggle chip switch, mic VU meter kicks in | Adjusts recording bus; long-press opens **Audio Mixer** (`SCREEN_11`). |
| **Orientation Selector** (`Auto` / `Portrait` / `Landscape`) | Single Tap | Phone rotation icon rotates 90° | Locks recording aspect ratio (`16:9` or `9:16`). |
| **Floating Ball Toggle Switch** | Toggle Tap | Switch thumb slides, system permission toast if not granted | Enables / disables Android system overlay permission. |
| **Recent Clips Carousel Items** | Single Tap | Card expands with ripple | Opens video in **Timeline Video Editor** (`SCREEN_29`). |
| **"View All Vault" Text Link** | Single Tap | Underline animation | Navigates to **Studio Vault & Media Library** (`SCREEN_24`). |

---

### B. Live Multistream Studio (`SCREEN_32`)

*Command center for multi-platform broadcasting.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Platform Toggles** (YouTube, Twitch, Kick, TikTok) | Toggle Tap | Checkmark fills with platform brand color; live follower count loads | Arms destination for simultaneous broadcast. |
| **"Add Custom RTMP / SRT" Button** (`+ Add`) | Single Tap | Modal fades in with upward slide | Opens **Custom RTMP & SRT Ingest Node Modal** (`SCREEN_13`). |
| **Camera PIP Preview Box** | Single Tap | Focus reticle appears, subtle border glow | Opens **Facecam & Chroma Key Studio** (`SCREEN_19`). |
| **Overlay / Alerts Config Button** (`🎨 Overlays`) | Single Tap | Haptic tick | Opens **Stream Alert & Widget Overlay Studio** (`SCREEN_2`). |
| **Audio Mixer Shortcut Button** (`🎚️ Mixer`) | Single Tap | Haptic tick | Opens **Pro Multi-Track Audio Mixer & DMCA Shield** (`SCREEN_11`). |
| **Encoder Settings Gear Icon** (`⚙️`) | Single Tap | 90° rotation animation | Opens **Broadcast Setup & RTMP Studio** (`SCREEN_22`). |
| **Primary "GO LIVE" CTA Button** | Single Tap | Glowing crimson pulse; pre-flight check triggers | Opens **Pre-Stream Go-Live Safety Checklist** (`SCREEN_4`). |

---

### C. Pre-Stream Go-Live Safety Checklist Modal (`SCREEN_4`)

*Pre-flight validation bottom-sheet before live transmission.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Modal Drag Handle / Close `✕`** | Swipe Down / Tap | Sheet slides down into bottom dismiss | Dismisses checklist, returns to **Live Multistream Studio**. |
| **"Enable DND Shield Now" Button** | Single Tap | Warning card turns emerald green ("DND Armed") | Invokes Android Do Not Disturb permission dialog. |
| **Broadcast Metadata "Edit" Link** | Single Tap | Edit pencil pulses | Expands title, game category, and audience tag editor. |
| **Mic Input VU Meter Test** | Voice Input | Real-time decibel peak meter reacts dynamically | Validates audio hardware pipeline. |
| **"Test Rehearsal (Unlisted)" Button** | Single Tap | Ghost button border pulses cyan | Starts unlisted private rehearsal stream without public notification. |
| **"LAUNCH LIVE MULTISTREAM" Button** | Single Tap | Deep haptic vibration, crimson pulse waves outward | Initiates live stream pipeline and opens **Live In-Game HUD & Overlay** (`SCREEN_18`). |

---

### D. Live In-Game HUD & Overlay (Full Viewport - `SCREEN_18`)

*Real-time HUD active during gaming and screen broadcasting.*

| Button / Gesture | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Floating Orb Core** | Single Tap | 6-slot radial menu fans out in 360° circle | Toggles in-game radial command wheel. |
| **Floating Orb Drag** | Drag & Release | Floats freely; auto-snaps with magnetic bounce to screen edge | Smooth physics docking to left or right margin. |
| **Floating Orb Double-Tap** | Double Tap | Screen edge flashes electric cyan, "AI Clip Buffered" HUD chip | Automatically saves last 30-second replay buffer to Vault. |
| **Floating Orb Long-Press (0.8s)** | Long Press | Heavy tactile haptic thud; mic icon slashes red | Instant hardware mic mute / unmute toggle. |
| **Floating Orb Swipe Inward** | Swipe Inward | Slides semi-transparent drawer from edge | Opens **Stream Live Chat & Unified Moderation Drawer** (`SCREEN_6`). |
| **Radial Slot 1: Pause / Stop** | Single Tap | Button switches from pause to stop square | Pauses recording; stopping opens **Instant Clip & Highlight Export** (`SCREEN_9`). |
| **Radial Slot 2: Screenshot** | Single Tap | White camera shutter flash across screen | Captures clean screenshot to Vault without HUD overlay. |
| **Radial Slot 3: Facecam PIP** | Single Tap | PIP window fades in / minimizes | Toggles live camera overlay window. |
| **Radial Slot 4: Brush / Doodle** | Single Tap | Bottom drawing palette slides in | Activates on-screen annotation pen, color picker, and eraser. |
| **Radial Slot 5: Soundboard / SFX** | Single Tap | Mini 4-pad SFX drawer expands | Plays instant live sound effects (*Airhorn*, *GG*, *Clutch*). |

---

### E. Stream Live Chat & Unified Moderation Drawer (`SCREEN_6`)

*Real-time aggregated stream chat drawer.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Close Drawer `←` / Backdrop** | Tap / Swipe Right | Drawer slides horizontally back into screen edge | Returns focus to active gaming screen / HUD. |
| **Platform Filter Pills** (`All`, `YouTube`, `Twitch`) | Single Tap | Sliding indicator pill highlights active selection | Filters chat stream by selected streaming platform. |
| **Pinned Super Chat "Send FX"** | Single Tap | Starburst particle effect on preview screen | Broadcasts visual celebration stinger on stream overlay. |
| **Spam Link Inline `[Approve]`** | Single Tap | Message card turns neutral, appears in public chat | Releases auto-held message to public stream. |
| **Spam Link Inline `[Ban User]`** | Single Tap | Card collapses, red confirmation badge | Bans bad actor across connected streaming channel API. |
| **Quick Shield Toggles** (`Slow`, `Sub-Only`, `Shield`) | Toggle Tap | Shield badge illuminates in cyan | Updates live chat room moderation constraints in real time. |
| **Chat Macro Chips** (`!rules`, `!specs`, `!discord`) | Single Tap | Macro text auto-fills into input field | Quick-sends automated streamer responses. |

---

### F. Floating Ball Customization & Gesture Binder (`SCREEN_8`)

*Interactive customization suite for the floating assist ball.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Back `←` / Close `✕`** | Single Tap | Smooth fade out | Returns to **Floating Ball & Settings** (`SCREEN_31`). |
| **Live Simulator Ball** | Drag & Drop | Demonstrates physics, edge dock, and opacity decay | Simulates real-time in-game experience. |
| **Idle / Active Opacity Sliders** | Drag Slider | Numeric percentage updates live; preview ball dims/brightens | Persists visual transparency preference. |
| **Docking Mode** (`Magnetic`, `Free Float`, `Half-Pill`) | Single Tap | Selected card borders in Crimson | Sets physics collision rule. |
| **Gesture Dropdowns** (Single, Double, Long-press) | Single Tap | Native selection sheet unfolds | Re-binds gestures to custom actions or macros. |
| **Radial Menu "Edit Slots" Button** | Single Tap | Slots pulsate in edit mode with `✕` remove badges | Allows reordering and swapping the 6 radial shortcuts. |
| **Orb Visual Skin Cards** (`Cyber Red`, `Stealth`, etc.) | Single Tap | Active radio checkmark shifts, orb previews new texture | Sets visual skin of floating assist orb. |
| **"Apply Orb Configuration" Button** | Single Tap | Success checkmark toast "Configuration Saved" | Saves settings and updates system overlay service. |

---

### G. Stream Alert & Widget Overlay Studio (`SCREEN_2`)

*Visual overlay layer manager.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Aspect Ratio Switcher** (`16:9` / `9:16`) | Single Tap | Canvas morphs smoothly between landscape and vertical | Adapts overlay layout for Twitch/YouTube vs TikTok/Shorts. |
| **Widget Toggles** (Follower Alert, Goal Bar, Chat Box) | Toggle Tap | Layer activates in preview canvas above with fade-in | Enables or disables widget in broadcast feed. |
| **"Trigger Test Alert" Button** | Single Tap | Mock follower animation bursts on preview with audio | Validates alert timing, graphic render, and chime volume. |
| **Minimum Tip Threshold Slider** | Drag Slider | Dollar amount scales (`$1.00` to `$100.00`) | Filters small spam donations from popping up on stream. |
| **Dynamic Goal Segmented Switch** (`Subs`, `Stars`, `Followers`) | Single Tap | Progress bar title and metric re-calculate | Changes target tracking source. |
| **Sound Pack Selector Chips** (`Cyber Synth`, `8-Bit`, etc.) | Single Tap | Audio preview plays immediately; active chip checks | Assigns sound effects library for alerts. |
| **"Publish Overlay to Stream Ingest"** | Single Tap | Glowing ripple, toast "Overlay Profile v3.4 Armed" | Syncs overlay layout to active live streaming pipeline. |

---

### H. Pro Multi-Track Audio Mixer & DMCA Shield (`SCREEN_11`)

*Independent 4-channel DSP mixing console.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Master Broadcast Fader** | Drag Fader | Master stereo VU peak meter reacts, dB value adjusts | Sets global stream mix volume. |
| **Channel Volume & Pan Sliders** (Game, Mic, Discord, Music) | Drag Slider | Precise dB readout updates; stereo pan indicator shifts | Fine-tunes individual channel gain and stereo position. |
| **Mic AI De-Noise / Compressor Toggles** | Single Tap | Pill illuminates cyan with "Active" badge | Engages real-time DSP noise cancellation and voice compression. |
| **Smart Ducking Slider** (`-40% on mic activity`) | Drag Slider | Ducking threshold animates | Automatically lowers game/music when streamer talks. |
| **DMCA Shield Matrix Grid Checkboxes** | Single Tap | Matrix highlights `Shield Active` | Routes copyright audio strictly to live stream and excludes from VODs. |
| **SFX Quick Deck Pads** (`Airhorn`, `Clutch Clap`, etc.) | Single Tap | Pad flashes active crimson; audio triggers immediately | Fires stream soundboard effects directly into the live feed. |
| **"Apply Audio Profile" Button** | Single Tap | Toast "Audio DSP Synchronized" | Commits mixer settings to hardware audio loopback bus. |

---

### I. Instant Clip & Highlight Export Flow (`SCREEN_9`)

*AI-assisted Shorts/TikTok clipping and export tool.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Close `←` Button** | Single Tap | Asks "Discard clip edits?" confirmation if modified | Returns to **Capture Hub** or **Studio Vault**. |
| **Framing Selector** (`9:16 Shorts`, `16:9 Wide`, `1:1`) | Single Tap | Video viewport snaps with spring animation | Re-frames video using smart AI player tracking. |
| **Timeline Trimmer Handles** | Horizontal Drag | Filmstrip frames expand; haptic tick at AI event markers | Sets custom clip in/out points with millisecond accuracy. |
| **AI Event Markers** (`Triple Kill`, `Victory Drop`) | Single Tap | Trimmer automatically snaps to event window | Auto-trims to most exciting highlight moments. |
| **Facecam PIP Docking Toggle & Scale Slider** | Toggle & Drag | PIP camera appears docked at top of 9:16 video | Formats streamer reaction video over mobile gameplay. |
| **Neon Subtitles & Kill Slow-Mo Switches** | Toggle Tap | Preview instantly renders glowing caption overlay | Adds engagement FX for vertical mobile feeds. |
| **Quick Share Chips** (TikTok, YT Shorts, Reels, Discord) | Single Tap | Opens Android native share intent for selected platform | Zero re-encoding instant social export. |
| **"Render & Export 9:16 Clip" Button** | Single Tap | Progress ring overlay with render speed indicator | Renders final MP4 video and saves to gallery. |
| **"Save to PulseCast Vault" Button** | Single Tap | Checkmark icon with cloud sync badge | Stores clip in local storage and cloud vault backup. |

---

### J. Custom RTMP & SRT Ingest Node Modal (`SCREEN_13`)

*Advanced broadcast transport protocol setup.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Transport Protocol Switcher** (`RTMP(S)`, `SRT Caller`, `RTSP`) | Single Tap | Tab pill slides, input fields dynamically reconfigure | Switches network transport engine. |
| **Preset Service Chips** (`Custom`, `Trovo`, `Bilibili`) | Single Tap | Auto-fills known server endpoints | Pre-populates default ingest URL. |
| **Masked Stream Key Eye Icon** (`👁️`) | Single Tap | Dots turn to readable alphanumeric string | Unmasks private stream key for verification. |
| **QR Code Scan Button** (`🔲`) | Single Tap | Camera viewfinder overlay opens | Scans stream key QR code from desktop screen. |
| **Bitrate Slider** (`8,500 Kbps`) | Drag Slider | Uplink overhead calculation updates (`68% utilized`) | Configures encoder target bitrate. |
| **"Test Handshake & Ping" Button** | Single Tap | Socket spinner animates, returns RTT ping (`18ms`) & jitter (`0.8ms`) | Verifies connection health before going live. |
| **"Connect & Arm Node" Button** | Single Tap | Button turns solid green with lock badge | Commits endpoint to **Live Multistream Studio** (`SCREEN_32`). |

---

### K. Creator Profile & Connected Channels Hub (`SCREEN_15`)

*Streamer identity, multi-platform accounts, and cloud vault.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Pro Studio "Manage" Button** | Single Tap | Opens Pro subscription tier sheet | Displays membership plan, renewal dates, and invoice history. |
| **Channel Account Cards** (YouTube, Twitch, TikTok) | Single Tap | Expands channel health, stream keys, and linked account settings | Manages OAuth credentials and channel permissions. |
| **"Add Ingest" Header Action** | Single Tap | Haptic click | Opens **Custom RTMP & SRT Ingest Node Modal** (`SCREEN_13`). |
| **Cloud Vault Breakdown Bar** | Single Tap | Expands segmented list (*Master VODs*, *Clips*, *Buffers*) | Navigates to **Studio Vault** filtered to cloud files. |
| **Auto-Sync & Wi-Fi Only Checkboxes** | Single Tap | Checkbox toggle with status feedback | Sets cloud background upload policies. |
| **Watermark Placement Anchor** (`TL`, `TR`, `BL`, `BR`) | Single Tap | Anchor dot shifts to selected quadrant in thumbnail | Sets persistent broadcast logo watermark location. |
| **"Disconnect / Log Out" Button** | Single Tap | Red confirmation dialog ("Are you sure?") | Unlinks local session and clears cached keys. |

---

### L. Performance & Stream Diagnostics Analytics (`SCREEN_17`)

*Hardware health, thermals, and network telemetry.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Time Range Pills** (`Real-Time`, `5m`, `15m`, `Session`) | Single Tap | Chart curves re-render with smooth bezier transition | Updates telemetry historical window. |
| **Diagnostics Metric Cards** (FPS, Uplink, SoC Temp, Battery) | Single Tap | Card expands with detailed subsystem recommendations | Shows throttling alerts or encoder buffer bottlenecks. |
| **"Auto-Optimize Encoder" CTA** | Single Tap | Telemetry radar scan animation, adjusts bitrates dynamically | Optimizes bitrate and resolution for current thermal headroom. |

---

### M. Studio Vault & Media Library (`SCREEN_24`)

*Storage management for recordings, clips, and screenshots.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Vault Category Tabs** (`Recordings`, `Clips`, `Screenshots`) | Single Tap | Underline tab slides, grid updates with filtered assets | Filters asset list. |
| **Media Card Thumbnail** | Single Tap | Expands full-screen player or image preview | Launches video playback with trim / edit options. |
| **Card "Edit" Scissor Icon** | Single Tap | Haptic click | Passes media file directly to **Timeline Video Editor** (`SCREEN_29`). |
| **Card "Share" Icon** | Single Tap | Native Android share drawer opens | Exports media directly to messaging or social apps. |
| **Select / Batch Multi-Select Button** | Single Tap | Circular checkboxes appear on every media card | Enables bulk actions (Delete, Cloud Upload, Compress). |

---

### N. Timeline Video Editor (`SCREEN_29`)

*Pro mobile timeline video editing suite.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Scrubber Playhead** | Drag Scrub | Video preview updates at exact frame; audio scrub plays | Frame-accurate positioning. |
| **Split Tool Button** (`✂️ Split`) | Single Tap | Clip slices at playhead into two separate blocks | Splits clip into independent segments. |
| **Speed Ramp Tool** (`⚡ Speed`) | Single Tap | Speed curve editor expands (0.25x to 4.0x) | Adjusts video playback velocity. |
| **Audio Track Fader** | Drag Slider | Audio envelope wave updates height | Balances background game sound vs voiceover. |
| **Export / Render Button** (`Export`) | Single Tap | Resolution & bitrate picker sheet modal slides in | Renders final edited master video to Vault. |

---

### O. Floating Ball & System Settings (`SCREEN_31`)

*Android system permissions and hardware capture toggles.*

| Button / Control | Trigger | Micro-Interaction | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **"Customize Floating Ball" Tile** | Single Tap | Chevron arrow pulses, right slide | Navigates to **Floating Ball Customization & Gesture Binder** (`SCREEN_8`). |
| **Permission Tiles** (Display Over Apps, Mic, Storage) | Toggle Tap | System permission prompt triggered if ungranted | Verifies Android background overlay compliance. |
| **Hardware Codec Picker** (`H.264`, `HEVC`, `AV1`) | Single Tap | Radio selector shifts | Toggles hardware GPU encoder acceleration. |

---

## 4. Master Navigation Hierarchy Diagram

```
PulseCast Studio Architecture
│
├── 1. Global App Bar Header
│   ├── [📊] ──> Performance & Stream Diagnostics Analytics (SCREEN_17)
│   ├── [👤] ──> Creator Profile & Connected Channels Hub (SCREEN_15)
│   └── [Storage] ──> Studio Vault & Media Library (SCREEN_24)
│
├── 2. Bottom Navigation Anchors
│   ├── [Tab: Record] ───────> Capture Hub (SCREEN_28)
│   │                          └── [● REC Button] ──> Live In-Game HUD & Overlay (SCREEN_18)
│   │
│   ├── [Tab: Live] ─────────> Live Multistream Studio (SCREEN_32)
│   │                          ├── [+ Add Custom RTMP] ──> Custom RTMP & SRT Ingest Node Modal (SCREEN_13)
│   │                          ├── [🎨 Overlays] ────────> Stream Alert & Widget Overlay Studio (SCREEN_2)
│   │                          ├── [🎚️ Audio Mixer] ─────> Pro Multi-Track Audio Mixer & DMCA Shield (SCREEN_11)
│   │                          ├── [Facecam PIP] ────────> Facecam & Chroma Key Studio (SCREEN_19)
│   │                          └── [GO LIVE CTA] ────────> Pre-Stream Go-Live Safety Checklist (SCREEN_4)
│   │                                                      └── [Launch] ──> Live In-Game HUD & Overlay (SCREEN_18)
│   │
│   ├── [Center: Floating Orb] ──> Floating Ball Customization & Gesture Binder (SCREEN_8)
│   │
│   ├── [Tab: Editor] ───────> Timeline Video Editor (SCREEN_29)
│   │                          └── [Shorts / AI Clip] ───> Instant Clip & Highlight Export Flow (SCREEN_9)
│   │
│   └── [Tab: Tools] ────────> Floating Ball & System Settings (SCREEN_31)
│
└── 3. Live Active Stream HUD (SCREEN_18)
    ├── [Swipe Inward / Chat] ─> Stream Live Chat & Unified Moderation Drawer (SCREEN_6)
    ├── [Stop Recording] ──────> Instant Clip & Highlight Export Flow (SCREEN_9)
    └── [Radial SFX / Mixer] ──> Pro Multi-Track Audio Mixer (SCREEN_11)
```

---

## 5. Engineering Implementation Guidelines

1. **Haptic Feedback Conventions**:
   - Light tick (`5ms`): Category chips, sliders, tab navigation, macro pills.
   - Medium click (`12ms`): Button clicks, toggle switches, menu slot fan-outs.
   - Heavy confirmation thud (`30ms`): Record start/stop, DND shield arming, go-live launch.
2. **Animation Curves**:
   - Drawers & Bottom Sheets: `cubic-bezier(0.16, 1, 0.3, 1)` (Spring exit/entrance, duration `280ms`).
   - Floating Orb Snapping: `spring(damping: 24, stiffness: 280)`.
   - Modals & Overlays: Backdrop fade `200ms ease-out`, card zoom `scale-95 to scale-100`.
3. **Design Tokens**:
   - Base Surface: `#0e131e`
   - Elevated Cards: `#171b27`
   - Primary Accent / Live: `#ff2d55`
   - Telemetry / Diagnostic Cyan: `#00f0ff`
   - Warnings & Super Chats: `#ffd600` / `#f59e0b`

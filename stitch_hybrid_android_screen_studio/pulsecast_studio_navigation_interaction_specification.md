# PulseCast Studio — Interaction Specification & Navigation Flow Architecture (.MD)

This document provides a comprehensive, engineering-ready specification of all **button clicks, interactive touch targets, micro-interactions, gestures, state changes, and target destinations** across the **PulseCast Studio** ecosystem (blending XRecorder & Prism Live Studio).

> **Note on Screen Identification**: Screen references below use the exact title, filename, and visual distinguishing features found in your project screens and canvas exports.

---

## Screen Title & Visual Reference Index

| Screen Title | Key Visual Features / In-Screen Banner | Primary Role |
| :--- | :--- | :--- |
| **Capture Hub** | Red `● REC` button, resolution pills (`1080p`, `60 FPS`), audio toggles | Home Recording Dashboard |
| **Live Multistream Studio** | Destination platform switches (YouTube, Twitch, Kick, TikTok), `GO LIVE` button | Live Broadcast Command Center |
| **Pre-Stream Go-Live Safety Checklist** | "96% FLIGHT READY" ring meter, DND shield warning card | Pre-Flight Verification Bottom Sheet |
| **Live In-Game HUD & Overlay** | Fullscreen 16:9 gameplay backdrop, circular floating assistive orb, bottom doodle bar | In-Game Overlay HUD |
| **Stream Live Chat & Unified Moderation Drawer** | Aggregated chat feed, "$50.00 Pinned Super Chat", spam filter actions | Host Chat & Moderation Drawer |
| **Floating Ball Customization & Gesture Binder** | "Live Physics Simulator" game screen, idle/active opacity sliders, 6-slot radial wheel | Assistive Orb Configuration |
| **Stream Alert & Widget Overlay Studio** | "Interactive Canvas Viewport" with 16:9/9:16 toggles, follower alert cards | Dynamic Stream Overlay & Alerts |
| **Pro Multi-Track Audio Mixer & DMCA Shield** | Master stereo VU meter (`-1.2 dB`), 4 channel strips, DMCA Shield routing grid | Audio Hardware DSP & Strike Protection |
| **Instant Clip & Highlight Export Flow** | 9:16 Shorts preview, precision trimmer timeline, AI Event markers | AI Shorts/TikTok Replay Editor |
| **Custom RTMP & SRT Ingest Node Modal** | "Add Ingest Target Node", protocol tabs (`RTMP(S)`, `SRT Caller`), ping test | Custom Transport Protocol Modal |
| **Creator Profile & Connected Channels Hub** | Streamer gamer avatar, Pro Studio membership card, Cloud Vault bar | Account, Channels & Cloud Hub |
| **Performance & Stream Diagnostics Analytics** | FPS/bitrate bezier curves, SoC thermals (`38.5°C`), battery telemetry | Real-Time Hardware Diagnostics |
| **Studio Vault & Media Library** | "Recordings / Clips / Screenshots" tabs, video thumbnail cards, batch select | Media Storage & Cloud Vault |
| **Timeline Video Editor** | Multi-track playhead scrubber, split/speed ramp buttons, track faders | Full Mobile Post-Production Editor |
| **Floating Ball & Settings** | System permission toggles ("Display over other apps"), codec selectors | App Settings & Android Permissions |
| **Facecam & Chroma Key Studio** | Camera preview, shape masks (Circle/Hexagon/Squircle), green screen chroma slider | Streamer Facecam & Green Screen Studio |
| **Broadcast Setup & RTMP Studio** | Scene templates, encoder bitrate slider, CBR/VBR encoding configuration | In-Depth Broadcast Configuration |

---

## 1. Global App Bar Header (Persistent Across Primary Screens)

Present on: **Capture Hub**, **Live Multistream Studio**, **Timeline Video Editor**, **Floating Ball & Settings**, **Studio Vault & Media Library**, **Broadcast Setup & RTMP Studio**, **Performance & Stream Diagnostics Analytics**, and **Creator Profile & Connected Channels Hub**.

| UI Element / Tap Target | Visual Cue | Micro-Interaction & Visual Feedback | Target Destination / Action |
| :--- | :--- | :--- | :--- |
| **Brand Logo & Wordmark** | `PULSECAST PRO` in top left with red pulse dot | Subtle scale-down (`active:scale-95`), haptic tick | Navigates to **Capture Hub** (Home root). |
| **Storage Indicator Badge** | `128 GB Free • ~18h 45m` pill | Glows subtle cyan, expands quick storage breakdown tooltip | Navigates directly to **Studio Vault & Media Library** (`Cloud Vault` tab). |
| **Diagnostics Bar Graph Icon** | Vertical bar chart icon (`📊`) in header | Haptic click, icon highlights in `#00f0ff` Cyan | Opens **Performance & Stream Diagnostics Analytics**. |
| **Creator Profile Avatar** | Circular avatar in top right with pink/red rim | Profile ring pulses `#ff2d55`, haptic feedback | Opens **Creator Profile & Connected Channels Hub**. |
| **Back Button Arrow** | `←` chevron on secondary and detail screens | Slide transition reverse, haptic tick | Returns current view back to previous screen or primary hub. |

---

## 2. Global Bottom Navigation Bar (5 Primary Anchor Targets)

| Tab Item | Visual Icon & State | Micro-Interaction | Screen / Destination |
| :--- | :--- | :--- | :--- |
| **Tab 1: Record** | Video camcorder icon | Icon tints active Crimson, subtle bounce animation | Navigates to **Capture Hub**. |
| **Tab 2: Live** | Broadcast antenna / radio signal waves | Red active pip appears below, glows Crimson | Navigates to **Live Multistream Studio**. |
| **Center FAB: Quick Orb** | Raised circular crimson button with Pulse target logo | Scale bounce (`scale-110`), haptic thud | Triggers **Floating Ball Customization & Gesture Binder** or toggles system-wide overlay. |
| **Tab 3: Editor** | Filmstrip clapper / scissor icon | Scissor snips animation, tints Crimson | Navigates to **Timeline Video Editor**. |
| **Tab 4: Tools** | Sliders / DSP equalizer icon | Equalizer bars bounce, tints Crimson | Navigates to **Floating Ball & Settings** or studio tools. |

---

## 3. Screen-by-Screen Interaction & Navigation Map

---

### A. Capture Hub (Home Screen)

*Visual Identifiers: Large crimson record button, resolution & FPS selector chips, recent recording carousel.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Big Crimson Record Button** (`● REC`) | Single Tap | 3-second countdown overlay (`3... 2... 1... GO!`), app collapses | Minimizes app and launches **Live In-Game HUD & Overlay**. |
| **Resolution Preset Chips** (`1080p` / `2K` / `4K`) | Single Tap | Active outline shifts crimson, haptic bump | Updates active capture profile instantaneously. |
| **FPS Selector Chips** (`60 FPS` / `120 FPS`) | Single Tap | Smooth background slide between options | Toggles high-refresh rate screen capture buffer. |
| **Audio Source Switcher** (`Internal` / `Mic` / `Mute`) | Single Tap | Toggle chip switch, mic VU meter kicks in | Adjusts recording bus; long-press opens **Pro Multi-Track Audio Mixer & DMCA Shield**. |
| **Orientation Selector** (`Auto` / `Portrait` / `Landscape`) | Single Tap | Phone rotation icon rotates 90° | Locks recording aspect ratio (`16:9` or `9:16`). |
| **Floating Ball Toggle Switch** | Toggle Tap | Switch thumb slides, system permission toast if not granted | Enables / disables Android system overlay permission. |
| **Recent Clips Carousel Items** | Single Tap | Card expands with ripple | Opens selected video in **Timeline Video Editor**. |
| **"View All Vault" Link** | Single Tap | Underline animation | Navigates to **Studio Vault & Media Library**. |

---

### B. Live Multistream Studio

*Visual Identifiers: Destination platform cards (YouTube, Twitch, Kick, TikTok), camera PIP preview, large "GO LIVE" button.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Platform Toggles** (YouTube, Twitch, Kick, TikTok) | Toggle Tap | Checkmark fills with platform brand color; live follower count loads | Arms destination for simultaneous broadcast. |
| **"Add Custom RTMP / SRT" Button** (`+ Add`) | Single Tap | Modal fades in with upward slide | Opens **Custom RTMP & SRT Ingest Node Modal**. |
| **Camera PIP Preview Box** | Single Tap | Focus reticle appears, subtle border glow | Opens **Facecam & Chroma Key Studio**. |
| **Overlay / Alerts Config Button** (`🎨 Overlays`) | Single Tap | Haptic tick | Opens **Stream Alert & Widget Overlay Studio**. |
| **Audio Mixer Shortcut Button** (`🎚️ Mixer`) | Single Tap | Haptic tick | Opens **Pro Multi-Track Audio Mixer & DMCA Shield**. |
| **Encoder Settings Gear Icon** (`⚙️`) | Single Tap | 90° rotation animation | Opens **Broadcast Setup & RTMP Studio**. |
| **Primary "GO LIVE" CTA Button** | Single Tap | Glowing crimson pulse; pre-flight check triggers | Opens **Pre-Stream Go-Live Safety Checklist**. |

---

### C. Pre-Stream Go-Live Safety Checklist

*Visual Identifiers: "Pre-Flight Checklist" title, circular "96% FLIGHT READY" rocket badge, yellow "Enable DND Shield Now" button.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Modal Drag Handle / Close `✕`** | Swipe Down / Tap | Sheet slides down into bottom dismiss | Dismisses checklist, returns to **Live Multistream Studio**. |
| **"Enable DND Shield Now" Button** | Single Tap | Warning card turns emerald green ("DND Armed") | Invokes Android Do Not Disturb permission dialog. |
| **Broadcast Metadata "Edit" Link** | Single Tap | Edit pencil pulses | Expands title, game category, and audience tag editor. |
| **Mic Input VU Meter Test** | Voice Input | Real-time decibel peak meter reacts dynamically | Validates audio hardware pipeline. |
| **"Test Rehearsal (Private Unlisted Test)"** | Single Tap | Ghost button border pulses cyan | Starts unlisted private rehearsal stream without public notification. |
| **"LAUNCH LIVE MULTISTREAM" Button** | Single Tap | Deep haptic vibration, crimson pulse waves outward | Initiates live stream pipeline and opens **Live In-Game HUD & Overlay**. |

---

### D. Live In-Game HUD & Overlay

*Visual Identifiers: Fullscreen mobile game action background, floating assist orb on the right margin, bottom telestrator/doodle bar.*

| Button / Gesture | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Floating Orb Core** | Single Tap | 6-slot radial menu fans out in 360° circle | Toggles in-game radial command wheel. |
| **Floating Orb Drag** | Drag & Release | Floats freely; auto-snaps with magnetic bounce to screen edge | Smooth physics docking to left or right margin. |
| **Floating Orb Double-Tap** | Double Tap | Screen edge flashes electric cyan, "AI Clip Buffered" HUD chip | Automatically saves last 30-second replay buffer to Vault. |
| **Floating Orb Long-Press (0.8s)** | Long Press | Heavy tactile haptic thud; mic icon slashes red | Instant hardware mic mute / unmute toggle. |
| **Floating Orb Swipe Inward** | Swipe Inward | Slides semi-transparent drawer from edge | Opens **Stream Live Chat & Unified Moderation Drawer**. |
| **Radial Slot 1: Pause / Stop** | Single Tap | Button switches from pause to stop square | Pauses recording; stopping opens **Instant Clip & Highlight Export Flow**. |
| **Radial Slot 2: Screenshot** | Single Tap | White camera shutter flash across screen | Captures clean screenshot to Vault without HUD overlay. |
| **Radial Slot 3: Facecam PIP** | Single Tap | PIP window fades in / minimizes | Toggles live camera overlay window. |
| **Radial Slot 4: Brush / Doodle** | Single Tap | Bottom drawing palette slides in | Activates on-screen annotation pen, color picker, and eraser. |
| **Radial Slot 5: Soundboard / SFX** | Single Tap | Mini 4-pad SFX drawer expands | Plays instant live sound effects (*Airhorn*, *GG*, *Clutch*). |

---

### E. Stream Live Chat & Unified Moderation Drawer

*Visual Identifiers: Top banner "Unified Chat Stream • 3 FEEDS SYNCED", glowing yellow "$50.00 Pinned Super Chat", spam filter approve/ban actions.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Close Drawer `←` / Backdrop** | Tap / Swipe Right | Drawer slides horizontally back into screen edge | Returns focus to active gaming screen / HUD. |
| **Platform Filter Pills** (`All`, `YouTube`, `Twitch`) | Single Tap | Sliding indicator pill highlights active selection | Filters chat stream by selected streaming platform. |
| **Pinned Super Chat "Send FX"** | Single Tap | Starburst particle effect on preview screen | Broadcasts visual celebration stinger on stream overlay. |
| **Spam Link Inline `[Approve]`** | Single Tap | Message card turns neutral, appears in public chat | Releases auto-held message to public stream. |
| **Spam Link Inline `[Ban User]`** | Single Tap | Card collapses, red confirmation badge | Bans bad actor across connected streaming channel API. |
| **Quick Shield Toggles** (`Slow`, `Sub-Only`, `Shield Bot`) | Toggle Tap | Shield badge illuminates in cyan | Updates live chat room moderation constraints in real time. |
| **Chat Macro Chips** (`!rules`, `!specs`, `!discord`) | Single Tap | Macro text auto-fills into input field | Quick-sends automated streamer responses. |

---

### F. Floating Ball Customization & Gesture Binder

*Visual Identifiers: "Live Physics Simulator" preview box, Idle/Active opacity sliders, 6-slot radial wheel mapper diagram, Orb skins.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Back `←` / Close `✕`** | Single Tap | Smooth fade out | Returns to **Floating Ball & Settings**. |
| **Live Simulator Ball** | Drag & Drop | Demonstrates physics, edge dock, and opacity decay | Simulates real-time in-game experience. |
| **Idle / Active Opacity Sliders** | Drag Slider | Numeric percentage updates live; preview ball dims/brightens | Persists visual transparency preference. |
| **Docking Mode** (`Magnetic`, `Free Float`, `Half-Pill`) | Single Tap | Selected card borders in Crimson | Sets physics collision rule. |
| **Gesture Dropdowns** (Single, Double, Long-press) | Single Tap | Native selection sheet unfolds | Re-binds gestures to custom actions or macros. |
| **Radial Menu "Edit Slots" Button** | Single Tap | Slots pulsate in edit mode with `✕` remove badges | Allows reordering and swapping the 6 radial shortcuts. |
| **Orb Visual Skin Cards** (`Cyber Red`, `Stealth Carbon`, etc.) | Single Tap | Active radio checkmark shifts, orb previews new texture | Sets visual skin of floating assist orb. |
| **"Apply Orb Configuration" Button** | Single Tap | Success checkmark toast "Configuration Saved" | Saves settings and updates system overlay service. |

---

### G. Stream Alert & Widget Overlay Studio

*Visual Identifiers: "Interactive Canvas Viewport" showing game screen with follower notification overlay, 16:9 Landscape / 9:16 Shorts switcher.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Aspect Ratio Switcher** (`16:9` / `9:16`) | Single Tap | Canvas morphs smoothly between landscape and vertical | Adapts overlay layout for Twitch/YouTube vs TikTok/Shorts. |
| **Widget Toggles** (Follower Alert, Goal Bar, Chat Box) | Toggle Tap | Layer activates in preview canvas above with fade-in | Enables or disables widget in broadcast feed. |
| **"Trigger Test Alert" Button** | Single Tap | Mock follower animation bursts on preview with audio | Validates alert timing, graphic render, and chime volume. |
| **Minimum Tip Threshold Slider** | Drag Slider | Dollar amount scales (`$1.00` to `$100.00`) | Filters small spam donations from popping up on stream. |
| **Dynamic Goal Segmented Switch** (`Subs`, `Stars`, `Followers`) | Single Tap | Progress bar title and metric re-calculate | Changes target tracking source. |
| **Sound Pack Selector Chips** (`Cyber Synth`, `8-Bit Arcade`, etc.) | Single Tap | Audio preview plays immediately; active chip checks | Assigns sound effects library for alerts. |
| **"Publish Overlay to Stream Ingest"** | Single Tap | Glowing ripple, toast "Overlay Profile Armed" | Syncs overlay layout to active live streaming pipeline. |

---

### H. Pro Multi-Track Audio Mixer & DMCA Shield

*Visual Identifiers: Large master bus VU needle meter (`-1.2 dB`), 4 track strips with sliders, "DMCA Shield Matrix" grid table, 6 SFX pads.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Master Broadcast Fader** | Drag Fader | Master stereo VU peak meter reacts, dB value adjusts | Sets global stream mix volume. |
| **Channel Volume & Pan Sliders** (Game, Mic, Discord, Music) | Drag Slider | Precise dB readout updates; stereo pan indicator shifts | Fine-tunes individual channel gain and stereo position. |
| **Mic AI De-Noise / Compressor Toggles** | Single Tap | Pill illuminates cyan with "Active" badge | Engages real-time DSP noise cancellation and voice compression. |
| **Smart Ducking Slider** (`-40% on mic activity`) | Drag Slider | Ducking threshold animates | Automatically lowers game/music when streamer talks. |
| **DMCA Shield Matrix Grid Checkboxes** | Single Tap | Matrix highlights `Shield Active` | Routes copyright audio strictly to live stream and excludes from VODs. |
| **SFX Quick Deck Pads** (`Airhorn`, `Clutch Clap`, etc.) | Single Tap | Pad flashes active crimson; audio triggers immediately | Fires stream soundboard effects directly into the live feed. |
| **"Apply Audio Profile" Button** | Single Tap | Toast "Audio DSP Synchronized" | Commits mixer settings to hardware audio loopback bus. |

---

### I. Instant Clip & Highlight Export Flow

*Visual Identifiers: Vertical 9:16 mobile gameplay preview with streamer webcam in top PIP, precision filmstrip trimmer, "Render & Export 9:16 Clip" button.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Close `←` Button** | Single Tap | Asks "Discard clip edits?" confirmation if modified | Returns to **Capture Hub** or **Studio Vault & Media Library**. |
| **Framing Selector** (`9:16 Shorts`, `16:9 Wide`, `1:1`) | Single Tap | Video viewport snaps with spring animation | Re-frames video using smart AI player tracking. |
| **Timeline Trimmer Handles** | Horizontal Drag | Filmstrip frames expand; haptic tick at AI event markers | Sets custom clip in/out points with millisecond accuracy. |
| **AI Event Markers** (`Triple Kill`, `Victory Drop`) | Single Tap | Trimmer automatically snaps to event window | Auto-trims to most exciting highlight moments. |
| **Facecam PIP Docking Toggle & Scale Slider** | Toggle & Drag | PIP camera appears docked at top of 9:16 video | Formats streamer reaction video over mobile gameplay. |
| **Neon Subtitles & Kill Slow-Mo Switches** | Toggle Tap | Preview instantly renders glowing caption overlay | Adds engagement FX for vertical mobile feeds. |
| **Quick Share Chips** (TikTok, YT Shorts, Reels, Discord) | Single Tap | Opens Android native share intent for selected platform | Zero re-encoding instant social export. |
| **"Render & Export 9:16 Clip" Button** | Single Tap | Progress ring overlay with render speed indicator | Renders final MP4 video and saves to gallery. |
| **"Save to PulseCast Vault" Button** | Single Tap | Checkmark icon with cloud sync badge | Stores clip in local storage and cloud vault backup. |

---

### J. Custom RTMP & SRT Ingest Node Modal

*Visual Identifiers: "Add Ingest Target Node" header, protocol pills (`RTMP(S)`, `SRT Caller`, `RTSP Relay`), masked key with QR scanner icon, ping speed test.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Transport Protocol Switcher** (`RTMP(S)`, `SRT Caller`, `RTSP`) | Single Tap | Tab pill slides, input fields dynamically reconfigure | Switches network transport engine. |
| **Preset Service Chips** (`Custom Server`, `Trovo`, `Bilibili`) | Single Tap | Auto-fills known server endpoints | Pre-populates default ingest URL. |
| **Masked Stream Key Eye Icon** (`👁️`) | Single Tap | Dots turn to readable alphanumeric string | Unmasks private stream key for verification. |
| **QR Code Scan Button** (`🔲`) | Single Tap | Camera viewfinder overlay opens | Scans stream key QR code from desktop screen. |
| **Bitrate Slider** (`8,500 Kbps`) | Drag Slider | Uplink overhead calculation updates (`68% utilized`) | Configures encoder target bitrate. |
| **"Test Handshake & Ping" Button** | Single Tap | Socket spinner animates, returns RTT ping (`18ms`) & jitter (`0.8ms`) | Verifies connection health before going live. |
| **"Connect & Arm Node" Button** | Single Tap | Button turns solid green with lock badge | Commits endpoint to **Live Multistream Studio**. |

---

### K. Creator Profile & Connected Channels Hub

*Visual Identifiers: Streamer gamer avatar with level badge, "Pro Studio Active" subscription card, channel cards with follower counts, Cloud Vault storage breakdown.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Pro Studio "Manage" Button** | Single Tap | Opens Pro subscription tier sheet | Displays membership plan, renewal dates, and invoice history. |
| **Channel Account Cards** (YouTube, Twitch, TikTok) | Single Tap | Expands channel health, stream keys, and linked account settings | Manages OAuth credentials and channel permissions. |
| **"Add Ingest" Header Action** | Single Tap | Haptic click | Opens **Custom RTMP & SRT Ingest Node Modal**. |
| **Cloud Vault Breakdown Bar** | Single Tap | Expands segmented list (*Master VODs*, *Clips*, *Buffers*) | Navigates to **Studio Vault & Media Library** filtered to cloud files. |
| **Auto-Sync & Wi-Fi Only Checkboxes** | Single Tap | Checkbox toggle with status feedback | Sets cloud background upload policies. |
| **Watermark Placement Anchor** (`TL`, `TR`, `BL`, `BR`) | Single Tap | Anchor dot shifts to selected quadrant in thumbnail | Sets persistent broadcast logo watermark location. |
| **"Disconnect / Log Out" Button** | Single Tap | Red confirmation dialog ("Are you sure?") | Unlinks local session and clears cached keys. |

---

### L. Performance & Stream Diagnostics Analytics

*Visual Identifiers: Real-time bezier graph showing FPS & bitrate stability, 4 metric cards (FPS, Uplink, SoC Temp, Battery), "Auto-Optimize Encoder" CTA.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Time Range Pills** (`Real-Time`, `5m`, `15m`, `Session`) | Single Tap | Chart curves re-render with smooth bezier transition | Updates telemetry historical window. |
| **Diagnostics Metric Cards** (FPS, Uplink, SoC Temp, Battery) | Single Tap | Card expands with detailed subsystem recommendations | Shows throttling alerts or encoder buffer bottlenecks. |
| **"Auto-Optimize Encoder" CTA** | Single Tap | Telemetry radar scan animation, adjusts bitrates dynamically | Optimizes bitrate and resolution for current thermal headroom. |

---

### M. Studio Vault & Media Library

*Visual Identifiers: Category tabs ("Recordings", "Clips", "Screenshots"), 2-column video grid with duration tags and resolution chips, batch multi-select button.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Vault Category Tabs** (`Recordings`, `Clips`, `Screenshots`) | Single Tap | Underline tab slides, grid updates with filtered assets | Filters asset list. |
| **Media Card Thumbnail** | Single Tap | Expands full-screen player or image preview | Launches video playback with trim / edit options. |
| **Card "Edit" Scissor Icon** | Single Tap | Haptic click | Passes media file directly to **Timeline Video Editor**. |
| **Card "Share" Icon** | Single Tap | Native Android share drawer opens | Exports media directly to messaging or social apps. |
| **Select / Batch Multi-Select Button** | Single Tap | Circular checkboxes appear on every media card | Enables bulk actions (Delete, Cloud Upload, Compress). |

---

### N. Timeline Video Editor

*Visual Identifiers: Horizontal video preview scrubber, dual multi-track audio/video lanes, split/speed ramp tools, red "Export" button.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Scrubber Playhead** | Drag Scrub | Video preview updates at exact frame; audio scrub plays | Frame-accurate positioning. |
| **Split Tool Button** (`✂️ Split`) | Single Tap | Clip slices at playhead into two separate blocks | Splits clip into independent segments. |
| **Speed Ramp Tool** (`⚡ Speed`) | Single Tap | Speed curve editor expands (0.25x to 4.0x) | Adjusts video playback velocity. |
| **Audio Track Fader** | Drag Slider | Audio envelope wave updates height | Balances background game sound vs voiceover. |
| **Export / Render Button** (`Export`) | Single Tap | Resolution & bitrate picker sheet modal slides in | Renders final edited master video to Vault. |

---

### O. Floating Ball & Settings

*Visual Identifiers: "Customize Floating Ball" tile with chevron, Android system permission cards with toggle switches, hardware codec radio selector.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **"Customize Floating Ball" Tile** | Single Tap | Chevron arrow pulses, right slide | Navigates to **Floating Ball Customization & Gesture Binder**. |
| **Permission Tiles** (Display Over Apps, Mic, Storage) | Toggle Tap | System permission prompt triggered if ungranted | Verifies Android background overlay compliance. |
| **Hardware Codec Picker** (`H.264`, `HEVC`, `AV1`) | Single Tap | Radio selector shifts | Toggles hardware GPU encoder acceleration. |

---

### P. Facecam & Chroma Key Studio

*Visual Identifiers: Streamer camera feed with shape clipping borders (Circle, Hexagon, Squircle), green screen chroma threshold sliders, rim light color palette.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Shape Preset Chips** (Circle, Hexagon, Squircle, Rect) | Single Tap | Camera feed mask smoothly morphs into selected geometry | Sets camera PIP outline shape. |
| **Chroma Key Toggle** | Toggle Tap | Background behind streamer turns transparent | Engages real-time GPU green screen keyer. |
| **Chroma Color Eyedropper** | Single Tap | Color loupe magnifies camera background pixels | Samples exact backdrop green tint. |
| **RGB Rim Glow Palette** | Single Tap | Selected color ring highlights around camera feed | Adds streamer edge light highlight. |

---

### Q. Broadcast Setup & RTMP Studio

*Visual Identifiers: Scene template presets, encoder bitrates (1,000 to 15,000 Kbps), CBR/VBR rate control toggles, GOP keyframe intervals.*

| Button / Control | Trigger | Visual Feedback | Target Destination / Resulting State |
| :--- | :--- | :--- | :--- |
| **Scene Layout Presets** (Game Only, Game + Cam, Cam Centric) | Single Tap | Thumbnail card highlights crimson | Loads pre-configured visual layout. |
| **Bitrate Slider** | Drag Slider | Numeric readout updates (`6,000 Kbps`) | Sets broadcast streaming bandwidth target. |
| **Rate Control Mode** (`CBR` / `VBR`) | Single Tap | Selected mode pill highlights | Switches between constant and variable bitrate encoding. |

---

## 4. Master Navigation Hierarchy Diagram

```
PulseCast Studio Architecture
│
├── 1. Global App Bar Header
│   ├── [📊 Diagnostics Icon] ──> Performance & Stream Diagnostics Analytics
│   ├── [👤 Profile Avatar] ────> Creator Profile & Connected Channels Hub
│   └── [128 GB Storage Pill] ──> Studio Vault & Media Library
│
├── 2. Global Bottom Navigation Anchors
│   ├── [Tab: Record] ──────────> Capture Hub
│   │                             └── [● REC Button] ───────> Live In-Game HUD & Overlay
│   │
│   ├── [Tab: Live] ────────────> Live Multistream Studio
│   │                             ├── [+ Add Custom RTMP] ──> Custom RTMP & SRT Ingest Node Modal
│   │                             ├── [🎨 Overlays] ────────> Stream Alert & Widget Overlay Studio
│   │                             ├── [🎚️ Audio Mixer] ─────> Pro Multi-Track Audio Mixer & DMCA Shield
│   │                             ├── [Facecam PIP] ────────> Facecam & Chroma Key Studio
│   │                             ├── [⚙️ Gear Icon] ───────> Broadcast Setup & RTMP Studio
│   │                             └── [GO LIVE CTA] ────────> Pre-Stream Go-Live Safety Checklist
│   │                                                         └── [Launch] ──> Live In-Game HUD & Overlay
│   │
│   ├── [Center: Quick Orb FAB] ─> Floating Ball Customization & Gesture Binder
│   │
│   ├── [Tab: Editor] ──────────> Timeline Video Editor
│   │                             └── [Shorts / AI Clip] ───> Instant Clip & Highlight Export Flow
│   │
│   └── [Tab: Tools] ───────────> Floating Ball & Settings
│                                 └── [Customize Orb] ──────> Floating Ball Customization & Gesture Binder
│
└── 3. Live Active Stream Viewport (Live In-Game HUD & Overlay)
    ├── [Swipe Inward / Chat] ──> Stream Live Chat & Unified Moderation Drawer
    ├── [Stop Recording] ───────> Instant Clip & Highlight Export Flow
    └── [Radial SFX / Mixer] ───> Pro Multi-Track Audio Mixer & DMCA Shield
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
3. **Design System Tokens**:
   - Base Surface: `#0e131e`
   - Elevated Cards: `#171b27`
   - Primary Accent / Live: `#ff2d55`
   - Telemetry / Diagnostic Cyan: `#00f0ff`
   - Warnings & Super Chats: `#ffd600` / `#f59e0b`

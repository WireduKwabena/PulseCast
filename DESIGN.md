---
name: Broadcast Obsidian
colors:
  surface: '#0e131e'
  surface-dim: '#0e131e'
  surface-bright: '#343945'
  surface-container-lowest: '#090e19'
  surface-container-low: '#171b27'
  surface-container: '#1b202b'
  surface-container-high: '#252a36'
  surface-container-highest: '#303541'
  on-surface: '#dee2f2'
  on-surface-variant: '#e6bcbd'
  inverse-surface: '#dee2f2'
  inverse-on-surface: '#2b303c'
  outline: '#ad8888'
  outline-variant: '#5d3f40'
  surface-tint: '#ffb3b5'
  primary: '#ffb3b5'
  on-primary: '#680019'
  primary-container: '#ff5167'
  on-primary-container: '#5b0015'
  inverse-primary: '#be0036'
  secondary: '#bdf4ff'
  on-secondary: '#00363d'
  secondary-container: '#00e3fd'
  on-secondary-container: '#00616d'
  tertiary: '#ffba38'
  on-tertiary: '#432c00'
  tertiary-container: '#c08600'
  on-tertiary-container: '#3a2600'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdada'
  primary-fixed-dim: '#ffb3b5'
  on-primary-fixed: '#40000c'
  on-primary-fixed-variant: '#920027'
  secondary-fixed: '#9cf0ff'
  secondary-fixed-dim: '#00daf3'
  on-secondary-fixed: '#001f24'
  on-secondary-fixed-variant: '#004f58'
  tertiary-fixed: '#ffdeac'
  tertiary-fixed-dim: '#ffba38'
  on-tertiary-fixed: '#281900'
  on-tertiary-fixed-variant: '#604100'
  background: '#0e131e'
  on-background: '#dee2f2'
  surface-variant: '#303541'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 38px
  headline-xl-mobile:
    fontFamily: Space Grotesk
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-telemetry-lg:
    fontFamily: JetBrains Mono
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 22px
    letterSpacing: -0.02em
  label-telemetry-md:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: -0.01em
  label-telemetry-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
  button-text:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  space-xxs: 0.125rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-base: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
  safe-bottom-nav: 5rem
  floating-margin: 1rem
---

## Brand & Style

This design system establishes a high-performance, studio-grade aesthetic engineered for Android mobile creators, competitive mobile gamers, and live stream broadcasters. The core ethos is precision, immediacy, and low-latency utility. It balances the tactical discipline of hardware broadcast mixers with the fluid, tactile ergonomics expected of modern flagship Android interfaces.

The emotional signature is dialed into focused control: the interface recedes entirely when recording gameplay or desktop environments, yet surfaces instant telemetry (bitrate, frame pacing, drop frames, audio clipping) with surgical clarity. The styling combines Dark Precision Engineering with frosted broadcast glassmorphism. Pure deep slate-blacks establish infinite dynamic contrast against vivid, luminous tally signals—instantly communicating recording states, live transmission health, and audio gain limits.

## Colors

The palette is engineered around an OLED-optimized layered black hierarchy, augmented by three distinct broadcast signal standards:

- **Surface Layers:**
  - `bg-base`: `#0A0D14` (Deep obsidian background, maximum contrast)
  - `surface-low`: `#121722` (Card and panel backgrounds, floating overlays)
  - `surface-mid`: `#1A2130` (Interactive surfaces, pill tracks, inactive toggles)
  - `surface-high`: `#242D40` (Hover/press states, popovers, floating control badges)
  - `stroke-subtle`: `rgba(255, 255, 255, 0.08)` (Tactile panel dividers and perimeter definition)

- **Signal Spectrum:**
  - **Electric Ruby (`#FF2D55`):** Reserved for recording state indicators, tally badges, active capture triggers, clip bookmarks, and critical error spikes.
  - **Cyber Cyan (`#00E5FF`):** Live streaming telemetry, active audio channels, primary action commitments, active sliders, and sync statuses.
  - **Neon Amber (`#FFB300`):** System load warnings, thermal pacing limits, buffer underruns, bit-rate throttle flags, and VIP superchat markers.
  - **Signal Green (`#00E676`):** Stable broadcast network indicator, optimal audio headroom (0dB to -6dB range).

## Typography

The type system implements a strict split between structural metadata, administrative content, and real-time streaming telemetry. 

- **Space Grotesk** serves as the display and section header engine, imbuing the studio with a sharp, tech-forward, high-contrast personality.
- **Inter** provides neutral, zero-distortion reading clarity across parameter lists, modal sheets, setting descriptions, and viewer chat streams.
- **JetBrains Mono** is enforced across all dynamic, volatile readouts: hardware timers (hh:mm:ss:ff), variable bitrates (kbps/Mbps), video render frame rates (FPS), internal audio decibels (VU meters), and dropped frame counts. Its monospaced figures eliminate tabular layout shift during rapid live updates.

## Layout & Spacing

The system enforces a dynamic 8pt base grid with a 4pt subgrid for compact telemetry chips and HUD elements.

- **Mobile Viewport (Portrait):** Fluid single-column layout with 16px lateral padding. Persistent bottom studio navigation bar (height: 64px) elevated over a transparent 16px bottom edge safe area.
- **Broadcast Viewport (Landscape / In-Game Capture):** Fullscreen perimeter dock model. Live HUD bars lock to horizontal edges with a safe margin of 24px to accommodate rounded screen corners, punch-hole cameras, and notch cutouts.
- **Floating Overlays & Pip (Picture-in-Picture):** Floating ball recorder controls snap dynamically to 8px-inset boundary grids along the vertical edges with automatic magnetic docking.

## Elevation & Depth

Hierarchy is established via layered dark slate depth rather than stark shadow dispersion, supplemented by light-emitting active halos:

- **Level 0 (Canvas Base):** Solid `#0A0D14`. Ground zero for camera feeds, preview viewports, and timeline workbenches.
- **Level 1 (Docked Containers & Surfaces):** `#121722` with a uniform 1px outline of `rgba(255, 255, 255, 0.08)`. Non-blurred.
- **Level 2 (Floating Broadcast HUDs & Bottom Sheets):** `#121722` at 85% opacity paired with `backdrop-filter: blur(16px)` and a top edge highlight of `rgba(255, 255, 255, 0.12)`.
- **Level 3 (Floating Ball Action Widget & Popovers):** `#1A2130` (90% opacity, 20px blur) with an omnidirectional ambient drop shadow: `0 8px 32px rgba(0, 0, 0, 0.72)`.
- **Signal Elevation (Active Glows):** When capture is engaged, elements do not merely brighten; they project a neon diffused aura. Recording nodes gain an outer drop-glow of `0 0 16px rgba(255, 45, 85, 0.45)`. Live streaming anchors project `0 0 16px rgba(0, 229, 255, 0.45)`.

## Shapes

The geometric identity is defined by modern industrial rounded corners, featuring full-capsule pill treatments for high-priority status widgets:

- **Standard Cards and Panels:** 8px (`rounded-md`) to 16px (`rounded-lg`) corner radii for structured control trays.
- **Live Widgets & Metrics Badges:** Pill-shaped (`rounded-full`, 9999px) for stream counters, time codes, and status tags to visually distinguish transient metrics from static configuration settings.
- **Floating Ball Widget:** Strictly circular (`shape-full`) with a concentrated 2px concentric inner rim to simulate an optical lens ring.

## Components

### Buttons & Trigger Controls
- **Record/Live Trigger Button:** Dual-state composite button. In standby: 64px circular button, 2px outer track, solid `#FF2D55` core with a white recording glyph. In active state: transitions into an obsidian rounded-square (`12px` radius) stop button framed by a rhythmic pulsing `#FF2D55` halo (1.5s ease-in-out glow wave).
- **Secondary Action Buttons:** High-density capsules built with `#1A2130`, a 1px border of `rgba(255, 255, 255, 0.08)`, and interactive press state dampening to `#242D40`.

### Pills & Tabs
- **Segmented Mode Selectors:** Encapsulated pill tray (`#0A0D14`) with a floating `#1A2130` sliding capsule thumb. Active selection utilizes high-contrast white text (`#FFFFFF`) while inactive states drop to muted slate (`#8A96AA`).

### Audio Mixer & Sliders
- **Fader Strips:** Vertical/horizontal gain faders featuring an 8px track with dual-zone color grading: `#00E5FF` below -6dB, transitioning to `#FFB300` up to -1dB, and clipping at `#FF2D55` (0dB and above). The thumb is a rounded rectangular pill with a center tactile notch.
- **VU Level Meters:** Segmented LED bar graphs using 2px high slices with 1px gaps, dynamically lit by real-time decibel calculations.

### Floating Action Ball (Widget)
- **Floating Launcher:** 48px circle with a semi-transparent glass substrate (`rgba(18, 23, 34, 0.85)`). Displays a mini tally status dot at the 1 o'clock quadrant. On tap, expands horizontally into a radial or pill dock revealing Quick Record, Screenshot, Camera Flip, and Draw/Annotate icons.

### Telemetry HUD Badges
- **Status Strip:** Compact horizontal HUD consisting of grouped monospace chips:
  - Bitrate: `Cyber Cyan` dot + `JetBrains Mono` numerical value (e.g., `6400 kbps`).
  - FPS: Dynamic green-to-amber badge depending on target lock (e.g., `60 FPS`).
  - Timer: Obsidian pill with Electric Ruby flashing separator (`00:14:32`).

### Timeline Tracks (Post-Capture Editor)
- **Editor Strip:** Dark timeline channel with integrated audio waveform previews (`#00E5FF` at 40% opacity). Scrubber playhead features an illuminated white line topped with an Electric Ruby inverted-teardrop needle.

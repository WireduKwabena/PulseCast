# PulseCast Studio

(Package/repo name still `screen-recorder` internally — see the note in
"UI: Jetpack Compose migration" below on package renaming.)

A free, open-source Android screen recorder — built to be more reliable
than XRecorder-class apps (no crashes on long recordings, no ads, no
premium paywall) with some Prism Live Studio-style creator features on the
roadmap.

## Status: Phase 1 + Phase 2 scaffold

This is the recording-pipeline foundation, not a finished app. What's here:

**Phase 1**
- ✅ Start / pause / resume / stop screen recording with mic audio
- ✅ Floating overlay controls
- ✅ Correct pause/resume (no frozen gap baked into the output — see
  `PtsAdjuster`)
- ✅ Scoped-storage-correct saving via MediaStore (Android 10+) with a
  legacy fallback for 9 and below
- ✅ Foreground service permissions correct for Android 14's stricter
  requirements

**Phase 2**
- ✅ Face-cam bubble (CameraX, front camera, circular, draggable,
  independently toggleable from the main screen)
- ✅ Touch visualization — toggles the OS "Show taps" developer option,
  with an honest fallback when the app doesn't have `WRITE_SECURE_SETTINGS`
  (see "Touch visualization" section below — this is a real platform
  limitation, not a shortcut I took)

**Phase 3**
- ✅ Multi-destination RTMP streaming architecture: one shared encoder
  pipeline fans out to the local file save AND any number of RTMP
  destinations simultaneously (`FanOutDistributor`), with each
  destination's connection independently tracked and reconnected
  (`DestinationConnection`, exponential backoff via `ReconnectBackoff`) —
  one destination dropping never affects any other, or local recording.
  74 unit tests across the streaming/ package, all passing, including the
  two properties that actually matter: per-destination independence and
  fan-out isolation when one sink throws.
- ✅ **Wired to a real RTMP library** — RootEncoder's low-level
  `RtmpClient` (`com.pedro.rtmp.rtmp.RtmpClient`), the class actually
  meant for feeding pre-encoded frames from an external source rather
  than the higher-level camera-capture classes most of the library's docs
  cover. `RootEncoderRtmpPublisher.kt`'s doc comment marks each method's
  confidence level precisely: `setVideoInfo`/`sendVideo`'s signatures are
  verified against a real 2.6.6-era code sample from the library's own
  issue tracker; `connect()`'s existence and the `ConnectChecker`
  interface are verified via a real stack trace and a real usage site;
  `setAudioInfo`/`sendAudio` are inferred by symmetry with the verified
  video methods, not independently confirmed — that pair is the one thing
  to double-check first if audio streaming doesn't compile as-is.
  Dependency pinned to 2.6.6 specifically (not the current latest 2.8.0)
  to match the version that evidence came from.
- Attempting to add a destination and go live right now should actually
  work, pending that one real-device compile-and-connect test this
  sandbox can't perform. If a destination fails to connect, the fan-out
  design means local recording is completely unaffected either way —
  that part was already directly tested.

**Filters (Phase 3, added after streaming)**
- ✅ Face-cam filter presets — None, Warm, Cool, Vivid, Vintage, B&W —
  applied live to the CameraX preview via `Media3Effect`
  (`androidx.camera.media3.effect`), CameraX's own confirmed bridge to
  media3-effect's color-adjustment classes (`HslAdjustment`, `Brightness`,
  `Contrast`, `RgbFilter`). Deliberately built entirely from these
  documented classes rather than a hand-rolled 4x4 `RgbMatrix` — that
  class's exact array layout convention (row- vs column-major) wasn't
  something verifiable without a live device, so composing only
  fully-documented building blocks sidesteps that uncertainty rather than
  guessing at it. Tap the ✨ button on the face-cam bubble to cycle
  presets — changes apply on the fly via `Media3Effect.setEffects()`,
  confirmed by its own docs to not require restarting the camera.
- Every preset's brightness/contrast/saturation values are unit-tested
  against media3's documented valid ranges (`[-1,1]` and `[-100,100]`
  respectively) — 8 additional checks in `CoreLogicTests.kt`.
- ⚠️ One real, flagged uncertainty: hue rotation *direction*. Warm uses
  -15°, Cool uses +15° as a best-effort guess — `HslAdjustment.adjustHue()`'s
  sign convention for "which way is warmer" wasn't confirmable without
  visually testing on a device. If they look swapped, flip both signs in
  `FilterPreset.kt`.
- Dependency version pairing (`camera-media3` + `media3-effect`, both
  pinned to 1.4.1) is worth double-checking in Android Studio — confirmed
  each artifact exists and does what's described, but not the exact
  cross-artifact patch-version compatibility.

Not yet built: internal audio capture, screen annotation/drawing,
background removal. Background removal (ML Kit selfie segmentation + real-
time compositing) remains deferred — it's substantial enough on its own
(on-device ML inference wired into the same effects pipeline as filters,
plus the actual mask compositing) to warrant its own pass rather than
being squeezed in alongside everything above.

## UI: Jetpack Compose migration + real design system

The app got a real name, icon, and full design spec (`DESIGN.md`,
"Broadcast Obsidian") partway through, plus ~17 designed screens (Google
Stitch output). Given the visual complexity — glassmorphism blur, gradient
rings, pulsing glow halos, dual-zone gradient sliders — the UI layer was
migrated from classic XML Views to **Jetpack Compose**. This touched
`MainActivity` and added a new `ui/` package; **every other file in the
project is completely unchanged** — `core/`, `encoder/`, `streaming/`, and
all three services are UI-framework-agnostic and needed zero modification.

**A real inconsistency in `DESIGN.md` itself:** it contains two different
color palettes that don't agree — a generic Material-3-style token set in
the YAML frontmatter, and a detailed, product-specific palette (Electric
Ruby, Cyber Cyan, Neon Amber, Signal Green) in the prose "Colors" section.
The prose section is what every component description throughout the rest
of the document actually references, so that's what's implemented in
`Color.kt` — the frontmatter's conflicting values are unused. Worth
knowing if you ever revisit `DESIGN.md` and wonder why the colors don't
match its own top section.

**Fonts are real, not placeholders.** Space Grotesk, Inter, and JetBrains
Mono were pulled directly from Google's own font repository
(github.com/google/fonts, OFL-licensed) and verified as valid TrueType
data with the expected weight axes before bundling — see `res/font/`. All
three are variable fonts (no static per-weight files exist in the source
repo); `Type.kt`'s doc comment covers the one real caveat: weight
interpolation isn't guaranteed on this project's minSdk 24 specifically
(Android 7.0/7.1), though it degrades gracefully rather than crashing.

```
ui/
  theme/
    Color.kt        The resolved (prose-section) Broadcast Obsidian palette
    Type.kt         Font families + exact DESIGN.md typography scale
    Shape.kt        Corner radius tokens
    Spacing.kt       Spacing scale tokens
    Theme.kt        PulseCastTheme — dark-only, no light variant (matches
                     the OLED-optimized design brief)
  components/
    PulseComponents.kt   PulseCard, PulsePill, TelemetryChip
  screens/
    RecordScreen.kt      The primary/home screen (Image 2 of the design set)
```

**Honest status on `RecordScreen.kt`:** structurally faithful to the
mockup and the only thing achievable without visual feedback — but I have
**zero ability to preview or run Compose UI in this sandbox** (no
emulator, no Compose Preview, no Compose compiler plugin even installed
here — only plain Kotlin syntax-checking, which can't validate
`@Composable` runtime semantics at all). Specifically not yet matching the
mockup: the circular storage indicator is a plain arc, not the exact
gradient ring shown; the record button doesn't have the pulsing glow
animation DESIGN.md specifies; the audio faders use a flat color instead
of the dual-zone cyan→amber→ruby gradient. These need your eyes on an
actual build, not more guessing from me.

**Scope decision on this pass:** face-cam, filters, touch visualization,
and stream-destination management (all fully working at the service
layer from Phases 1-3) have no UI entry point in `MainActivity` anymore.
The actual design puts that configuration on a dedicated Tools/Settings
screen (Image 4, "Capture Studio Config") that doesn't exist as a Compose
screen yet — that's genuinely the next screen to build, not a regression.
Given 17 total screens, this was built one screen at a time rather than
all at once, matching how every other phase of this project was paced.

**Package renaming:** the Kotlin package is still `com.kwabena.screenrecorder`
and the repo/README title line still says so in one spot — a full rebrand
to something like `com.kwabena.pulsecaststudio` is a mechanical, low-risk
change (touches every file's package declaration) that was deliberately
left for later rather than bundled into an already-large UI migration.

## Touch visualization — a real platform limitation, not a shortcut

There is no public Android API for a third-party app to draw touch
indicators over *other* apps' screens system-wide. What this — and every
legitimate screen recorder — actually does is toggle the OS's own "Show
taps" developer option (`Settings.System.SHOW_TOUCHES`). That requires
`WRITE_SECURE_SETTINGS`, a signature-level permission no app can be granted
through a normal permission dialog. To actually enable it during
development or personal use:

```bash
adb shell pm grant com.kwabena.screenrecorder android.permission.WRITE_SECURE_SETTINGS
```

Without that grant, tapping "Enable Touch Visualization" in the app
gracefully falls back to opening Developer Options so the user can flip it
on by hand.

## Why native Kotlin, not Flutter

Screen recording leans entirely on low-level platform APIs —
`MediaProjection`, `MediaCodec`, `MediaMuxer`, foreground service types.
Flutter can wrap these via a plugin, but you'd be dependent on plugin
quality for the exact pieces (pause/resume, overlay windows) that are
hardest to get right. Native Kotlin gives full control, at the cost of it
being new territory.

## Architecture

```
core/                  Pure Kotlin, zero Android imports — unit-testable
                        in plain Kotlin/JVM without an emulator
  RecordingStateMachine.kt   Explicit state transitions; rejects invalid
                             moves (double-pause, stop-while-idle, etc.)
                             instead of letting them corrupt a recording
  PtsAdjuster.kt             Fixes the "pause bakes in a frozen gap" bug
  RecordingConfig.kt         Bitrate/resolution calculation, with an
                             even-dimension guard (odd dimensions crash
                             several OEM hardware encoders)
  FilterPreset.kt            Pure filter preset parameters (hue/saturation/
                             brightness/contrast per preset) — see
                             service/FaceCamFilterEffects.kt for where
                             these become real media3 Effect objects
  SaveLocation.kt            MediaStore-based save, scoped-storage correct

encoder/               Android-specific — needs a device/emulator
  VideoEncoder.kt            MediaCodec Surface-input mode, async callbacks
  AudioEncoder.kt            AudioRecord -> MediaCodec AAC encoding
  MuxerWrapper.kt            Fixes the "muxer started before both tracks
                             registered" race condition

streaming/              Pure Kotlin (mostly) — the Phase 3 fan-out layer
  StreamDestination.kt       Destination + ConnectionState data types
  ReconnectBackoff.kt        Exponential backoff math
  DestinationConnection.kt   Per-destination state machine
  EncodedSample.kt /
  FanOutDistributor.kt       Encoding-agnostic sample type + fan-out with
                             per-sink failure isolation
  NalUnitParser.kt           Extracts SPS/PPS from Annex-B H.264, needed
                             for RTMP without depending on MediaFormat
  RtmpDestinationSink.kt     One RTMP destination as an EncodedSampleSink
  RawRtmpPublisher.kt        Our own interface — everything above is
                             written and tested against THIS, not against
                             any specific RTMP library
  RootEncoderRtmpPublisher.kt  The one file that's Android + library
                             specific — a stub, see its doc comment

encoder/               Android-specific
  VideoEncoder.kt / AudioEncoder.kt   Now fan out to a list of sinks
                             (local muxer + any live RTMP destinations),
                             not a single MuxerWrapper directly
  MuxerWrapper.kt            Fixes the "muxer started before both tracks
                             registered" race condition
  MuxerSink.kt               Adapts MuxerWrapper to the generic
                             EncodedSampleSink interface

service/               Android-specific
  ScreenRecordService.kt     Foreground service tying it all together
  OverlayControlsService.kt  Floating pause/resume/stop bubble
  FaceCamOverlayService.kt   Floating circular front-camera bubble
                             (CameraX, own foreground service — see
                             comments in the file for why it needs to be
                             its own foreground service, not just riding
                             along in ScreenRecordService's process).
                             Also wires up filter presets via Media3Effect.
  FaceCamFilterEffects.kt    Turns a FilterPreset into real media3 Effect
                             objects (HslAdjustment, Brightness, Contrast,
                             RgbFilter) — the Android-specific bridge for
                             filters, mirroring MuxerSink's role for the
                             streaming fan-out layer

MainActivity.kt         Permission flow using modern ActivityResult APIs,
                        plus adding stream destinations before recording
```

## Running this

Requires Android Studio (Koala or newer) with an SDK supporting API 34.

```bash
git clone <this repo>
```
Open the folder in Android Studio, let Gradle sync, run on a device or
emulator (screen recording needs a real display — most emulators support
`MediaProjection` fine, but audio capture behaves more reliably on a
physical device).

## Testing the core logic right now, no Android Studio needed

The `core/` package has zero Android dependencies specifically so it can
be tested without an emulator:

```bash
kotlinc app/src/main/kotlin/com/kwabena/screenrecorder/core/*.kt \
        app/src/test/kotlin/com/kwabena/screenrecorder/core/CoreLogicTests.kt \
        -include-runtime -d /tmp/tests.jar
java -jar /tmp/tests.jar
```

26 checks: state machine transition rules, the pause/resume timestamp
correction, and bitrate/resolution sanity + the even-dimension guard.

The Phase 3 streaming layer has its own suite, same idea:

```bash
kotlinc app/src/main/kotlin/com/kwabena/screenrecorder/streaming/*.kt \
        app/src/main/kotlin/com/kwabena/screenrecorder/core/TrackType.kt \
        app/src/test/kotlin/com/kwabena/screenrecorder/streaming/StreamingTests.kt \
        -include-runtime -d /tmp/streaming_tests.jar
java -jar /tmp/streaming_tests.jar
```

47 checks: reconnect backoff escalation, per-destination connection state
transitions (including that Twitch dropping never affects YouTube's
state), fan-out isolation (one throwing sink never blocks delivery to
others), NAL unit parsing for both 3- and 4-byte Annex-B start codes, and
the full RtmpDestinationSink lifecycle against a fake publisher.

Note: `RootEncoderRtmpPublisher.kt` needs excluding from this compile
command (it imports `android.util.Log`, which won't resolve outside
Android Studio) — the command above already only lists the files that
don't depend on it.

## License

Not yet set — given the goal of keeping this free forever, GPL-3.0 is
worth considering over MIT: it prevents someone taking this code and
shipping a closed-source, ad-laden fork under a different name, which MIT
would technically allow. Add a `LICENSE` file with whichever you land on
before the first public commit.

## Known gaps / next steps

- **Pause doesn't actually stop encoding new frames yet** — the
  VirtualDisplay keeps feeding the encoder while paused, which the encoder
  will just re-encode as a static frame repeatedly (wastes bitrate, though
  it doesn't corrupt anything since PtsAdjuster still fixes the output
  timeline). Proper fix: detach/reattach the VirtualDisplay's surface on
  pause/resume instead.
- **Internal audio capture** (`AudioPlaybackCapture`, Android 10+) isn't
  wired in — only mic audio currently.
- **No UI for resolution/bitrate/quality selection** — `Quality.MEDIUM` is
  hardcoded in `ScreenRecordService`.
- **Notification icon is a placeholder** (`android.R.drawable.presence_video_online`)
  — swap for a real app icon asset.
- **The control bubble (pause/resume/stop) has no way to be excluded from
  the recording itself.** MediaProjection with `VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR`
  mirrors everything visually composited on the real display, overlay
  windows included — that's what makes the face-cam bubble show up in the
  recording (intentional), but it also means your own control buttons show
  up too (usually not intentional). There's no stable pre-Android-14 public
  API to selectively exclude one overlay window from capture. Worth
  revisiting once minSdk can reasonably move past whatever exclusion API
  Android eventually stabilizes, or by making the control bubble small/
  edge-docked enough that it's an acceptable trade-off.
- **Face-cam isn't tied to the recording session** — it's a separate toggle
  on the main screen. Wiring it to auto-start/stop alongside
  `ScreenRecordService` would make more sense, but was left independent
  for now to keep this phase's diff reviewable.

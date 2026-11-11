# NEXUS Launcher — Development Roadmap

NEXUS is a customized Android-first Minecraft Java launcher based on this Battly Mobile 2 fork.

## Project goals
- Preserve the existing Minecraft launch engine and supported mod loaders.
- Rebrand the application as NEXUS Launcher while retaining required upstream notices and LGPL-3.0 compliance.
- Add a user-selectable launcher background from local device storage without a subscription gate.
- Keep and improve version, mod, resource-pack, shader-pack, and touch-control management.
- Add low-, medium-, and high-performance presets after device capability detection.
- Target Android first; evaluate a separate Windows client after Android is stable.

## Safety and implementation rules
- Work on nexus-development; do not modify v3_openjdk directly.
- Do not include signing keys, API secrets, or private credentials.
- Do not claim a feature works until it builds and has been tested on a device.
- Preserve upstream copyright notices and comply with the repository's LGPL-3.0 license.

## Initial implementation order
1. Inspect application ID, manifest, resources, and existing branding references.
2. Apply NEXUS app label and visual identity without changing package/runtime behavior.
3. Trace the existing background picker and remove only the Battly Plus restriction for local image selection.
4. Build a debug APK and test installation and Minecraft startup.

## Progress update — 2026-11-10

### Added on nexus-development
- NEXUS application label and initial project documentation.
- Free local image background selection from Android's document picker, with persistent local copy and preview; animated Plus-only backgrounds remain optional upstream functionality.
- NEXUS MUSIC initial Spotify PKCE connection and launcher mini-player controls (not yet build/device verified).
- NEXUS performance selector in Video and renderer settings:
  - Automatic tier uses available RAM and CPU core count.
  - Low tier applies 50% render resolution and sustained-performance mode.
  - Medium tier applies 75% render resolution and sustained-performance mode.
  - High tier applies 100% render resolution and disables sustained-performance limiting.
  - Profiles do not silently change the selected Java runtime or renderer.

### Existing engine features retained from Battly Mobile 2
- Minecraft Java version/runtime management and existing launch flow.
- Existing mod-loader and mod/resource-pack/shader management.
- Existing customizable touch controls and control marketplace.
- Existing background selection infrastructure.

### Remaining work, in order
1. Build the Android debug APK and fix all compile/resource errors.
2. Test install, version/runtime download, and first Minecraft launch on a real Android device.
3. Verify free custom background selection and persistence.
4. Test each performance tier on representative low-, mid-, and high-end devices; tune thresholds based on results.
5. Rebrand remaining visible Battly UI/assets and add NEXUS / “Creado por Joel” credits while preserving upstream license notices.
6. Test control marketplace downloads/imports and shader/resource-pack flows end-to-end.
7. Finish Spotify account disconnect, playlist browsing and robust error handling.
8. Begin Windows client planning only after Android launch is stable.

No APK build or physical-device test is claimed by this progress update.

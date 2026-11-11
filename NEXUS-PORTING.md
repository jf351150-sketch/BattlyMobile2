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

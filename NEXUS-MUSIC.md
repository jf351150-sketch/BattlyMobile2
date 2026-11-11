# NEXUS MUSIC — Spotify integration plan

## Product behavior
NEXUS MUSIC is an in-launcher music panel for Android. It must let the user:
- Connect and disconnect their own Spotify account.
- Browse the playlists returned for that account and open a playlist in Spotify.
- See current track, artist, artwork, playback state and context.
- Use play/pause, next, previous and supported playback controls.
- Keep Spotify playing while Minecraft Java is in the foreground, where Android and Spotify permit background playback.
- Show a compact mini-player inside the NEXUS launcher.
- While Minecraft is in the foreground, rely on Spotify's Android media notification, lock-screen and system media controls. Do not promise that NEXUS's in-launcher panel is visible over the game; avoid requesting Android overlay permission for the first version.
- Keep local NEXUS favorites/recently opened playlist references and a local queue of Spotify URIs, subject to Spotify API rules.

## Supported architecture
Use Spotify's official Android App Remote SDK for playback control and player-state updates. Playback audio remains inside the official Spotify app; NEXUS must not extract, proxy, cache, re-stream, modify, or mix Spotify audio.
Use Spotify Authorization Library / OAuth authorization code with PKCE for Web API access to the user's profile and playlists. Request only the scopes needed for playlist read and playback control. Store refresh/access tokens using Android Keystore-backed encrypted storage; never commit tokens or client secrets.
The Spotify Android app must be installed and the user must authorize NEXUS for App Remote. The integration must detect missing Spotify, expired authorization, unavailable network, and unsupported playback actions and show actionable states.

## Known platform constraints
- App Remote controls the Spotify app; it is not a standalone Spotify audio engine.
- Spotify App Remote requires the Spotify app on the Android device.
- Some on-demand playback operations require Spotify Premium. Check user capabilities and surface the limitation instead of promising unsupported playback.
- Web API access and playback-control endpoints have account, scope, and platform requirements. Validate each endpoint and scope against current Spotify documentation before implementation.
- Spotify developer policy prohibits commercial streaming integrations, altering Spotify content, synchronizing Spotify audio with visual media, and non-interactive broadcasting. NEXUS must not overlay visualizations synchronized to Spotify audio or bundle Spotify playback with Minecraft video capture/streaming.
- Do not use deprecated Spotify mobile streaming SDKs.

## Implementation phases
1. Add a dedicated NEXUS MUSIC feature package and UI state model without touching Minecraft runtime code.
2. Add the official SDK artifacts and OAuth PKCE flow after the app's Spotify Developer Dashboard client ID and Android redirect URI are configured.
3. Implement account connection, playlist retrieval, App Remote connection, player-state subscription and basic controls.
4. Add the launcher mini-player and verify Spotify's system media controls remain available while Minecraft runs on real Android hardware.
5. Add robust disconnect, token expiry, no-Spotify-installed, offline, permission-denied and non-Premium capability states.

## Configuration required before live Spotify authentication
- Spotify Developer Dashboard application client ID (public identifier, not a client secret).
- Registered Android package name and redirect URI matching the final NEXUS application identity.
- Android signing certificate SHA-1 fingerprint(s) registered in Spotify Dashboard.
- Spotify Android SDK artifacts added from Spotify's official distribution and their license/notice retained.

Never place private credentials, user tokens, signing keys, or refresh tokens in Git.

## Android implementation status (2026-11-10)

The Android branch now contains an initial Web API integration:

- NexusSpotifyAuth.java: Authorization Code with PKCE, random verifier/challenge and state validation, token exchange, refresh-token renewal, and AES-GCM token encryption backed by Android Keystore.
- LauncherActivity.java and AndroidManifest.xml: route the registered com.tecnobros.battlylauncher://callback URI into the launcher.
- MainMenuFragment.java and fragment_launcher.xml: show a NEXUS MUSIC mini-player with connect, previous, play/pause, and next controls.
- Playback requests use Spotify Web API and the scopes user-read-playback-state and user-modify-playback-state.

Limitations before release:
- This implementation has not yet been built or tested on a physical Android device.
- Playback control endpoints require Spotify Premium and an active Spotify Connect device. Spotify's API does not provide audio streaming to NEXUS; playback remains in Spotify.
- The current mini-player is in the launcher home screen, not an in-game Minecraft overlay.
- The user must authorize NEXUS again after installing a build that requests the additional playback scopes.
- Re-check Spotify's current Developer Mode access limits and policies before distributing the app.

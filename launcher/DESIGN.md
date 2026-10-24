---
version: alpha
name: Evening Home
description: A quiet dusk landscape and readable app rail for a remote-controlled MXQ TV.
colors:
  primary: "#D8E9F3"
  background: "#101C29"
  surface: "#1E3041"
  text: "#F1F4F5"
  muted: "#B6C5D0"
  focus: "#D8E9F3"
  sun: "#E4B586"
  ridge: "#47717B"
  foreground: "#244553"
typography:
  display:
    fontFamily: "Roboto Light, sans-serif"
    fontSize: "48px"
  body:
    fontFamily: "Roboto, sans-serif"
    fontSize: "19px"
  control:
    fontFamily: "Roboto Medium, sans-serif"
    fontSize: "17px"
rounded:
  DEFAULT: "10px"
spacing:
  safe-x: "48px"
  safe-y: "28px"
  rail-gap: "16px"
components:
  app-tile:
    width: "168px"
    height: "142px"
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
  button:
    height: "48px"
    textColor: "{colors.text}"
  focus:
    textColor: "{colors.focus}"
    size: "3px"
  secondary-text:
    textColor: "{colors.muted}"
  landscape-sun:
    backgroundColor: "{colors.sun}"
  landscape-ridge:
    backgroundColor: "{colors.ridge}"
  landscape-foreground:
    backgroundColor: "{colors.foreground}"
---

# Evening Home design

## Overview

The owner prefers a classic, simple TV interface and supplied
https://www.pinterest.com/pocketshopapp/smart-tv-ui/ as inspiration. The board
returned HTTP 403 during research on 2026-10-24; no exact pin was inspected or
copied. Apply the explicit direction through quiet underline navigation, flatter
rectangular tiles, generous wallpaper space, short labels and continuous shading.

The reference is a cinema screen viewed from the sofa. A slow succession of real
movie artwork fills the background; the controls stay familiar and still.
The user's job is to open an installed Android app using a TV remote.

This is a separate native Android launcher project. The adjacent financial web
application has no shared visual or behavioral ownership. The confirmed device is
an API-25 MXQ box with approximately 1 GB RAM and a 1280×720 dp display. Product
register: a household TV utility. English copy follows this conversation; labels
from installed applications and the clock retain the device's locale. No market
or location personalization is inferred.

Avoid advertising shelves, invented film recommendations, glass blur, autoplaying
video and tiny phone controls. The initial implementation
is an English landscape experience, not a claim of multilingual TV certification.

Runtime owner: `src/dev/mxq/home/Tokens.java`. This document and the browser review
preview mirror it. CSS px in this contract map to native dp for geometry and sp
for type, equal at the target device's density/font scale of 1. Home has no display heading; the wallpaper is the visual focus. The Apps
heading is 32sp in both implementations. `tests/check_contract.py` checks the shared palette.

## Colors

Background and surface carry the controls. Text and muted text are both readable
on the opaque app surfaces. Focus uses a 3dp pale-blue border and a lighter fill;
selected navigation uses a quiet 2dp underline. Warm sun is expressive only, never
an error/status indicator. Scene colors never become action backgrounds. A light horizontal scrim preserves the image; stronger top/bottom scrims
protect navigation and app labels. This version has one dark theme.

## Typography

Use platform sans-serif-medium for controls and system sans for body text.
Installed fonts supply script fallbacks. Labels are 17sp, section titles 22sp
and clock 20sp. No welcome headline, promotional copy, instruction footer or
product watermark appears on Home. App labels wrap to two
lines; accessibility names retain the full label. Text is never baked into art.

## Layout

TV-first landscape: 48dp horizontal and 28dp vertical safe padding. A 60dp header,
flexible wallpaper area, 54dp rail heading and 162dp horizontal favorite rail
fit the confirmed 720dp viewport. Home exposes real app entry points and two
destinations, Home and Apps, plus a quiet Recents utility action. The app grid has six columns at widths >=1000dp and
four below that; grid scrolls vertically, favorites horizontally. Neither depends
on a mouse. Portrait phones are outside the APK's supported layout. The desktop
preview scales the entire 16:9 TV frame to the browser width.

## Elevation & Depth

Depth comes from bundled studio movie imagery behind a horizontal dark scrim and
separate header/rail scrims. Controls use opaque tonal surfaces and borders, with
no shadows, blur, live video or scaling animation. Movie frames crossfade for
900ms after a 15-second dwell. Frames decode off the UI thread, capped at 1600×900
with RGB565 requested; only current/incoming frames are referenced. Timers and
fades stop while paused, detached or in the background. System animation scale
zero disables automatic rotation and fading; Next scene still swaps instantly.
The motion preference persists. Installed icons are small thumbnails loaded off
the UI thread. TVMaze metadata/images are fetched on a separate worker while
resumed; no wallpaper background service. The optional, user-enabled Recents service only opens the system task switcher. The vector
hillside remains a missing-asset fallback.

## Shapes

Controls share a restrained 10dp radius. App icons retain their publishers' shapes. Focus
changes border thickness inside the existing bounds, so the rail never jumps.

## Components

The canonical action and tile primitives live in MainActivity: `action`, `tile`,
`wire` and `controlBackground`. Every actionable view is focusable and has a
visible focus and pressed state. Hover only appears in the browser illustration;
Android uses standard remote/touch focus behavior. Selected tabs use an underline; header and rail utility actions stay transparent
until focus/hover/press. App tiles retain opaque surfaces. Selection stays distinct
from the current focus. Task dismissal and Force stop remain Android-owned actions reached through Recents and App settings; the launcher never stops apps automatically.

Home shows installed favorites with a small Favorites label, Next scene and Edit.
Home starts/restores focus to an app tile; Home navigation remains available when
no favorites are selected. Edit has the accessible name Edit favorites. A compact
64dp music bar sits immediately above the rail only after audio is detected.
It has play/pause, next and hide controls, with full accessible names. It keeps
its place while paused and reserves space without moving the app row. There are
no track titles, cover art, fake progress, media feeds or permission prompts.
Apps enumerates both TV and regular Android launch activities, preferring the TV
entry for duplicate packages. Native icons and labels come from PackageManager.
Loading, no apps, no favorites and enumeration failure all have honest text;
failures expose Retry. Launch refusal produces a native toast and reloads apps.

The header includes an accessible Wi-Fi icon and connected/disconnected label.
The APK reads only Android Wi-Fi network connection state, not SSID or credentials;
selecting it opens the system Wi-Fi settings. The preview simulates both states.
Pointer input has hover/pressed feedback and a hand cursor; favorites support
horizontal trackpad scrolling and vertical wheel fallback. D-pad remains primary.

Settings, favorite selection and About use platform AlertDialog, whose geometry,
focus handling and accessibility are intentionally Android-owned. Favorite changes
commit only on Save; Cancel discards the draft. Hold OK toggles a favorite; the
visible Edit button provides a non-shortcut alternative. Settings offers
the original launcher as an explicit destination and a pause/resume slideshow
action. Home includes Next scene; the current wallpaper title is available in Settings. Clock follows
the system format. Background changes never move focus or other controls.

The fallback landscape and product icon are original vector artwork. Movie
wallpapers are real publisher images fetched from official studio sites, with
provenance, credits and hashes in assets/wallpapers/catalog.json. Actual app icons
come from installed apps. The browser preview now uses the six favorite-app
icon resources extracted read-only from their installed APKs; other catalogue
behavior remains illustrative. Reduced-motion mode freezes automatic rotation. Content names
actions directly: Apps, Edit, Save favorites, Next scene,
Pause wallpaper slideshow.

## Online backgrounds

The owner selected TVMaze without an API key after sharing
https://github.com/z9m/androidtvbackgroundWebGui. That project is a separate
server/editor; this launcher uses the official public API directly. Online mode
is enabled by default for this requested update. Settings can return to the four
bundled movie wallpapers, refresh now, inspect source/status and open TVMaze.
No new shelf, title, rating or description is added to Home.

OnlineWallpapers owns the cache, network lifecycle and source policy;
OnlineWallpaperPolicy owns URL, dimension, freshness and retry limits. Five
curated action/animation show galleries supply landscape backdrops. Their IDs,
names and attribution URLs live in assets/wallpapers/tvmaze-shows.json. Refresh
once per24h while resumed, or explicitly in Settings; failures have a30min retry
cooldown. Network work cancels on pause/destroy. Retain saved artwork and bundled
fallbacks on failure. Keep focus/layout fixed when adopting a refreshed catalogue.

Cache at most8 images, each limited to3MiB compressed; old/new catalogues can
coexist briefly during a refresh. The renderer still decodes only current/incoming
frames capped at1600×900. Use verified HTTPS only, fixed API host and a validated
TVMaze image-CDN allowlist; reject posters, banners, unsafe URLs and oversized
images. Store provenance/source links with cached public metadata. Credit TVMaze
(CC BY-SA data) in Settings/About and preserve image owners' rights. No accounts,
keys, ads, app-list upload or listening history. INTERNET is the only new normal
permission. The browser starts in bundled mode for an offline review; its Settings
can fetch TVMaze, and automated provider tests use explicit fixtures.

## Recent apps

The owner requested access to tasks left behind after pressing Home. A single
Recents header action preserves the uncluttered layout and uses the shared action
primitive. Native AlertDialog explains optional setup or a failed handoff. Android
owns task cards, thumbnails and dismissal rather than duplicating a manager UI.
App settings is a Settings destination for choosing an installed app and opening
its Android details page. No running-app count, fake process status or task shelf
is added to Home. The browser dialog clearly describes a native-only handoff.

## Music controls

MusicControls owns native transport and lifecycle. AudioManager.isMusicActive
checks for audio once per second only while the activity is resumed. The bar
sends paired media-key down/up events to Android’s current media receiver. It
does not start or host playback; the music app must keep playing when Home opens.
Track skipping and resume depend on the player’s media-key support. Hide returns
focus to the app row; returning Home can show controls again. No notification
listener, extra permission, root, album download or background service is added.
The browser has an explicitly labelled simulation outside the TV frame.

## Do's and Don'ts

- Do give focus, readable labels and working app launch priority over decoration.
- Do retain the original launcher and make Home activation a separate opt-in.
- Don't imply this updates Android, improves DRM or fixes incompatible apps.
- Don't add online recommendations, tracking, root access or a wallpaper service.

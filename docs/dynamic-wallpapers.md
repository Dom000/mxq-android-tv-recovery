# Fresh online Home backgrounds — TVMaze

The owner selected TVMaze without an API key on2026-10-24. Evening Home0.3.0 is
installed and already fetched8 real backgrounds on the guarded MBOX. A native
Home screenshot displays Batman artwork from this catalogue. Default Home remains
Evening Home. This supersedes the earlier bundled-only wallpaper implementation.

## Project inspected

[z9m/androidtvbackgroundWebGui](https://github.com/z9m/androidtvbackgroundWebGui)
is a Python/web editor and background generator, designed for a separate server
or Docker host. It integrates media libraries and TMDB, generates images and
has a Projectivy plugin. Its README lists a dedicated Android TV app as planned.
It is not an APK replacement for this launcher. The inspected TMDB.py uses a
TMDB bearer token and trending movie/TV endpoints. No repository code was executed,
no public .env/token was read or reused, and no external server was started.

Inspected file blobs: README a7bd34daacf073ffdcf3fca05f25fb067bb9712d;
TMDB.py8a740c9bd5cb1a6bbcf24a54f6f518f42b44a783. MIT-licensed reference; the new
native provider is original platform Java code. Repository metadata is in
research/androidtvbackground-repo.json.

## API choice

[TMDB movie images](https://developer.themoviedb.org/reference/movie-images)
provides movie backdrops/posters/logos, but requires the owner's own API
credentials. [Registration](https://developer.themoviedb.org/docs/getting-started)
is through account settings. Do not take keys from public repositories.

[TVMaze's official public API](https://www.tvmaze.com/api) supplies show artwork
without a user account/key. We use /shows/{id}/images and type=background with
landscape dimensions, not poster thumbnails. The curated gallery IDs are in
launcher/assets/wallpapers/tvmaze-shows.json. Avatar uses the animated2005 show
(id555), resolved by its IMDb ID; ambiguous name search had initially returned
the live-action series and was corrected before packaging.

The API documents HTTPS, caching, rate limits and CC BY-SA data attribution.
Settings/About link to TVMaze. Cached catalogue entries retain image/page URLs
and data-license attribution. Image copyrights remain with their owners.

## Implemented behavior

- Fetch new shuffled artwork daily while Home is resumed, or select Settings →
  Refresh online backgrounds. Changes do not require a new APK.
- Mix action and animation galleries; cache up to8 images. Three MiB maximum per
  compressed image, with old/new files temporarily coexisting during replacement.
- Use two decoded frames, RGB565 requested, capped at1600×900. Keep15-second dwell,
  gentle crossfades, manual Next, pause and system reduced-motion handling.
- Retain current/cached/bundled art on network failure. No blank loading screen or
  extra Home text. Source, status, refresh and bundled-only controls stay in Settings.
- Fixed API host plus validated HTTPS image CDN URLs, response/image-size bounds,
  image-dimension checks, atomic cache metadata and stale-file pruning. No custom
  certificate trust, cleartext fallback or redirect to arbitrary hosts.
- No key/account, separate server, root or background service. Requests cancel on
  pause and destroy. No favorite-app names or listening history uploaded.

## Verification

Host policy tests cover unsafe host/user-info/port/path URLs, dimensions, cache
freshness, failure cooldown, clock rollback and forced refresh. Browser fixture
tests cover online artwork selection, poster/unsafe URL rejection, failure
retention and return to bundled art. They are not a live-browser-service claim.

Native0.3.0 installed at193304+0100. At193417+0100, Home showed a downloaded Batman
background. At193539+0100, the filtered own-tag log reported TVMaze cache refreshed:
8 images. HTTPS/API/CDN fetch and on-device display are therefore observed.
A real Wi-Fi-off/cache restart test and long-term memory/network measurements
remain pending; no network settings were changed. Source+schema+limits are also
recorded in launcher/UX-CONTRACT.md.

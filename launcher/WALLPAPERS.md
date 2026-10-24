# Movie wallpaper sources

Fetched 2026-10-24 from publicly accessible official publisher pages. The bundled
files are actual studio images. Full image URLs, SHA-256 hashes and byte counts:
[catalog.json](assets/wallpapers/catalog.json). Copyright remains with the respective
studios; the original artwork is retained.

| Wallpaper | Official source | Credit |
|---|---|---|
| Fast X | [Universal Pictures At Home](https://www.universalpicturesathome.com/movies/fast-x) | Universal Pictures |
| Avengers: Endgame | [Disney Movies gallery](https://movies.disney.com/avengers-endgame) | Marvel Studios |
| Spider-Man: Across the Spider-Verse | [Sony Pictures](https://www.sonypictures.com/movies/spidermanacrossthespiderverse) | Sony Pictures / Marvel |
| Kung Fu Panda 4 | [DreamWorks](https://www.dreamworks.com/movies/kung-fu-panda-4) | DreamWorks Animation / Universal Pictures |

Universal, Sony and DreamWorks supply promotional artwork; Disney supplies an
Endgame film frame. Browser/native views crop at render time to fill the 16:9
display, aligned right, and add dark overlays for legible launcher controls.
The source files are unmodified. No generated characters, trailers, full movies,
tracking scripts, studio HTML or accounts are packaged in the launcher.

## Online TVMaze backgrounds

Version0.3.0 adds an optional online cache, enabled for the owner-selected update.
Source: https://www.tvmaze.com/ and https://www.tvmaze.com/api. Data is CC BY-SA;
image rights remain with their owners. Each cached catalogue entry retains its
image URL and show sourcePage. Curated shows and source links are in
assets/wallpapers/tvmaze-shows.json. Online artwork is downloaded on the device,
not included in this APK or the four-image studio manifest above. Settings/About
credit TVMaze and link to the source. The browser test fixture separately records
its real Arcane image provenance in preview/fixtures/README.md.

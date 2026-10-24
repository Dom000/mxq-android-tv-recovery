package dev.mxq.home;

import java.net.URI;

/** Limits applied to public API data before fetching or decoding an image. */
final class OnlineWallpaperPolicy {
    static final int MAX_IMAGES = 8;
    static final int MAX_IMAGE_BYTES = 3 * 1024 * 1024;
    static final long REFRESH_MS = 24 * 60 * 60 * 1000L;
    static final long RETRY_MS = 30 * 60 * 1000L;

    static boolean imageUrl(String value) {
        try {
            URI uri = new URI(value);
            return "https".equals(uri.getScheme()) && "static.tvmaze.com".equals(uri.getHost())
                && uri.getUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443)
                && uri.getFragment() == null && uri.getQuery() == null
                && uri.getPath().matches("/uploads/images/[a-zA-Z0-9_/-]+\\.(jpg|jpeg|png|webp)");
        } catch (Exception invalid) { return false; }
    }

    static boolean dimensions(int width, int height) {
        double ratio = (double) width / Math.max(1, height);
        return width >= 960 && height >= 540 && width <= 8192 && height <= 8192
            && (long) width * height <= 20000000 && ratio >= 1.4 && ratio <= 2.0;
    }

    static boolean due(long now, long success, long attempt, boolean force) {
        if (force) return true;
        // A wrong/changed device clock must not disable updates indefinitely.
        return (success <= 0 || now < success || now - success >= REFRESH_MS)
            && (attempt <= 0 || now < attempt || now - attempt >= RETRY_MS);
    }

    private OnlineWallpaperPolicy() { }
}

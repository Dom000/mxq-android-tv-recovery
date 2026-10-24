package dev.mxq.home;

public final class OnlineWallpaperPolicyTest {
    private static void require(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        String good = "https://static.tvmaze.com/uploads/images/original_untouched/374/935123.jpg";
        require(OnlineWallpaperPolicy.imageUrl(good), "TVMaze original image accepted");
        for (String bad : new String[] {
            "http://static.tvmaze.com/uploads/images/original/1/2.jpg",
            "https://static.tvmaze.com.evil.test/uploads/images/original/1/2.jpg",
            "https://static.tvmaze.com@127.0.0.1/uploads/images/original/1/2.jpg",
            "https://user@static.tvmaze.com/uploads/images/original/1/2.jpg",
            "file:///data/data/private.jpg", "https://127.0.0.1/uploads/images/1/2.jpg",
            "https://static.tvmaze.com:8443/uploads/images/1/2.jpg",
            "https://static.tvmaze.com/uploads/images/../private.jpg",
            good + "?redirect=http://localhost", good + "#fragment", "invalid"
        }) require(!OnlineWallpaperPolicy.imageUrl(bad), "Untrusted image rejected: " + bad);
        require(OnlineWallpaperPolicy.dimensions(1280, 720), "HD landscape accepted");
        require(OnlineWallpaperPolicy.dimensions(3840, 2160), "4K may be sampled down");
        for (int[] size : new int[][] {{720,1280},{0,720},{1280,0},{800,450},{1500,500},{8192,8192}})
            require(!OnlineWallpaperPolicy.dimensions(size[0],size[1]), "Portrait/tiny/banner/oversized image rejected");
        long day = OnlineWallpaperPolicy.REFRESH_MS, now = day * 10;
        require(!OnlineWallpaperPolicy.due(now, now-1000, now-1000, false), "Fresh cache avoids network");
        require(OnlineWallpaperPolicy.due(now, now-day, now-day, false), "Old cache refreshes");
        require(!OnlineWallpaperPolicy.due(now, 0, now-1000, false), "Failure cooldown avoids Home retry loop");
        require(OnlineWallpaperPolicy.due(now, now+day, now+day, false), "Clock rollback recovers");
        require(OnlineWallpaperPolicy.due(now, now, now, true), "Explicit refresh bypasses freshness/cooldown");
        System.out.println("Online wallpaper URL isolation, image bounds and refresh/cooldown cases passed");
    }
}

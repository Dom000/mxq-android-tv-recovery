package dev.mxq.home;

public final class TaskIconIndexTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        TaskIconIndex index = new TaskIconIndex();
        index.add("Spotify", "com.spotify.tv.android");
        index.add("Spotify TV", "com.spotify.tv.android");
        index.add("Spotify", "com.spotify.tv.android"); // Same app's duplicate entry is safe.
        check("com.spotify.tv.android".equals(index.uniquePackage("  SPOTIFY ")));
        check("com.spotify.tv.android".equals(index.uniquePackage("Spotify TV")));
        index.add("Play   Store", "com.android.vending");
        check("com.android.vending".equals(index.uniquePackage("Play Store")));
        index.add("Settings", "com.android.settings");
        index.add("Settings", "com.android.tv.settings");
        check(index.uniquePackage("Settings") == null); // Never guess between matching brands.
        check(index.uniquePackage("Spotify playlist") == null); // Task/document titles aren't fuzzy aliases.
        check(index.uniquePackage(null) == null && index.uniquePackage("") == null);
        index.add("", "ignored.package"); index.add("Other", null);
        check(index.uniquePackage("Other") == null);
        index.add("Ｆｉｌｅｓ", "com.example.files");
        check("com.example.files".equals(index.uniquePackage("Files")));
        System.out.println("Task icons: exact aliases, duplicate entries, ambiguous names, unknown tasks and normalized labels passed");
    }
}

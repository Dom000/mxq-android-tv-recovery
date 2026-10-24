package dev.mxq.home;

import java.util.Arrays;
import java.util.Collections;

/** Meaningful host checks for persistence and package-removal edge cases. */
public final class AppOrderTest {
    public static void main(String[] args) {
        check(AppOrder.availablePins(Arrays.asList("brave", "youtube"), Arrays.asList("youtube", "brave"))
            .equals(Arrays.asList("brave", "youtube")), "saved ordering survives app enumeration");
        check(AppOrder.availablePins(Arrays.asList("removed", "brave", "brave", ""), Arrays.asList("brave"))
            .equals(Arrays.asList("brave")), "remove missing packages, duplicates and empty stored entry");
        check(AppOrder.availablePins(Collections.emptyList(), Arrays.asList("brave")).isEmpty(),
            "an intentionally empty favorite list stays empty");
        check(AppOrder.availablePins(Arrays.asList(AppOrder.SEEDS), Collections.emptyList()).isEmpty(),
            "seeded apps need to be installed");
        System.out.println("4 ordering/persistence cases passed");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

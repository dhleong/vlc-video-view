package net.dhleong.vlcvideoview;

import java.util.ArrayList;

/**
 * @author dhleong
 */
class VLCOptions {

    private static boolean verboseMode = BuildConfig.DEBUG;
    private static int networkCaching = 20000;

    static ArrayList<String> get() {
        ArrayList<String> options = new ArrayList<>(50);

        if (networkCaching > 0) {
            options.add("--network-caching=" + networkCaching);
        }

//        options.add("--android-display-chroma");
//        options.add("RV32"); // or YV12 or RV16
        options.add(verboseMode ? "-vv" : "-v");
        return options;
    }
}

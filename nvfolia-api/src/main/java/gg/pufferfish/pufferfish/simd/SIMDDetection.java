package gg.pufferfish.pufferfish.simd;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import org.slf4j.Logger;

/**
 * Global SIMD support state and detection entry point.
 */
// NOTE: Public Pufferfish API name, renaming would break plugins that link against it
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class SIMDDetection {
    /**
     * Whether SIMD operations are enabled.
     */
    public static boolean isEnabled = false;
    /**
     * Whether SIMD was disabled because the running Java version is unsupported.
     */
    public static boolean versionLimited = false;
    /**
     * Whether the SIMD probe has been run.
     */
    public static boolean testRun = false;

    /**
     * The newest Java feature version SIMD support is enabled for.
     */
    public static final int MAX_JAVA_VERSION = 25;
    /**
     * The oldest Java feature version SIMD support is enabled for.
     */
    public static final int MIN_JAVA_VERSION = 21;

    private SIMDDetection() {
    }

    /**
     * Checks whether SIMD operations can be enabled on this system.
     *
     * @param logger the logger to report the detected vector sizes to
     * @return {@code true} if SIMD operations are functional
     */
    public static boolean canEnable(Logger logger) {
        try {
            SIMDChecker checker = new SIMDChecker(IntVector.SPECIES_PREFERRED, FloatVector.SPECIES_PREFERRED);
            return checker.canEnable(logger);
        } catch (NoClassDefFoundError | Exception _) {
            return false;
        }
    }

    /**
     * Gets the feature version of the running Java runtime.
     *
     * @return the Java feature version, for example {@code 25}
     */
    public static int getJavaVersion() {
        // https://stackoverflow.com/a/2591122
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        version = version.split("-")[0];
        return Integer.parseInt(version);
    }
}

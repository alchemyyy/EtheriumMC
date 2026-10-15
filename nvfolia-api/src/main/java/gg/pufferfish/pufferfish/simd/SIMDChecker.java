package gg.pufferfish.pufferfish.simd;

import jdk.incubator.vector.VectorSpecies;
import org.slf4j.Logger;

/**
 * Probes whether the Vector API is usable for the given vector species.
 */
// NOTE: Public Pufferfish API name, renaming would break plugins that link against it
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SIMDChecker {
    private final VectorSpecies<Integer> intSpecies;
    private final VectorSpecies<Float> floatSpecies;

    /**
     * Creates a checker for the given vector species.
     *
     * @param intSpecies   the integer vector species to probe
     * @param floatSpecies the float vector species to probe
     */
    public SIMDChecker(VectorSpecies<Integer> intSpecies, VectorSpecies<Float> floatSpecies) {
        this.intSpecies = intSpecies;
        this.floatSpecies = floatSpecies;
    }

    /**
     * Checks whether SIMD operations can be enabled on this system.
     *
     * @param logger the logger to report the detected vector sizes to
     * @return {@code true} if SIMD operations are functional
     */
    public boolean canEnable(Logger logger) {
        try {
            if ((SIMDDetection.getJavaVersion() < SIMDDetection.MIN_JAVA_VERSION || SIMDDetection.getJavaVersion() > SIMDDetection.MAX_JAVA_VERSION)) {
                return false;
            } else {
                SIMDDetection.testRun = true;

                logger.info("Max SIMD vector size on this system is {} bits (int)", this.intSpecies.vectorBitSize());
                logger.info("Max SIMD vector size on this system is {} bits (float)", this.floatSpecies.vectorBitSize());

                if (this.intSpecies.elementSize() < 2 || this.floatSpecies.elementSize() < 2) {
                    logger.warn("SIMD is not properly supported on this system!");
                    return false;
                }

                return true;
            }
        } catch (NoClassDefFoundError | Exception _) {
            // Basically, we don't do anything. This lets us detect if it's not functional and disable it.
        }

        return false;
    }
}

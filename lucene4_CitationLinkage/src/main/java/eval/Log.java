package eval;


/**
 * Shared utility functions for computing log in base-2.
 */
class Log {

    /**
     * Returns the base-2 logarithm of {@code d}.
     */
    public static double log2(double d) {
        return Math.log(d) / Math.log(2);
    }

    /**
     * Returns the base-2 logarithm of {@code d + 1}.
     * 
     * @see Math#log1p(double)
     */
    static double log2_1p(double d) {
        return Math.log1p(d) / Math.log(2);
    }
}

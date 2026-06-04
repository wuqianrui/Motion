package com.motion.utils;

public class AtmosUtils {
    private static final double STANDARD_TEMP = 288.15;
    private static final double STANDARD_PRESSURE = 101325.0;
    private static final double LAPSE_RATE = 0.0065;

    public static double calculatePressureAltitude(double pressure) {
        return 44330.0 * (1.0 - Math.pow(pressure / STANDARD_PRESSURE, 0.1903));
    }

    public static double calculateDensity(double pressure, double temperature) {
        return (pressure * 0.0289644) / (8.31446 * temperature);
    }
}

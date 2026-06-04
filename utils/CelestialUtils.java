package com.motion.utils;

public class CelestialUtils {
    private static final double J2000 = 2451545.0;

    public static double daysSinceJ2000(long timestampMs) {
        return (timestampMs / 86400000.0) + 2440587.5 - J2000;
    }

    public static double calculateJulianDate(long timestampMs) {
        return (timestampMs / 86400000.0) + 2440587.5;
    }
}

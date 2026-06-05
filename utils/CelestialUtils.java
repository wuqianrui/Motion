package com.motion.utils;

public class CelestialUtils {
    private static final double J2000 = 2451545.0;

    public static double daysSinceJ2000(long timestampMs) {
        return (timestampMs / 86400000.0) + 2440587.5 - J2000;
    }

    public static double calculateJulianDate(long timestampMs) {
        return (timestampMs / 86400000.0) + 2440587.5;
    }

    public static double[] calculateSolarPosition(long timestampMs, double lat, double lon) {
        double jd = calculateJulianDate(timestampMs);
        double n = jd - 2451545.0;
        double L = (280.460 + 0.9856474 * n) % 360;
        double g = Math.toRadians((357.528 + 0.9856003 * n) % 360);
        double lambda = L + 1.915 * Math.sin(g) + 0.020 * Math.sin(2 * g);
        double declination = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(lambda)) * 0.39782));
        double eqTime = 4 * Math.toDegrees(Math.asin(0.01745 * Math.sin(g)));
        double tc = 4 * (lon - eqTime);
        double hourAngle = tc - 720;
        return new double[]{declination, hourAngle};
    }

    public static double calculateMoonPhase(long timestampMs) {
        double jd = calculateJulianDate(timestampMs);
        double days = jd - 2444238.0;
        double phase = (days % 29.530588853) / 29.530588853;
        return phase;
    }
}
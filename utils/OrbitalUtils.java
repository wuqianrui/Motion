package com.motion.utils;

public class OrbitalUtils {
    private static final double MU = 3.986004418e14;

    public static double calculateOrbitalPeriod(double semiMajorAxis) {
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / MU);
    }

    public static double calculateVelocity(double r, double a) {
        return Math.sqrt(MU * (2.0 / r - 1.0 / a));
    }
}

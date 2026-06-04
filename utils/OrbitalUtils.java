package com.motion.utils;

public class OrbitalUtils {
    private static final double MU = 3.986004418e14;

    public static double calculateOrbitalPeriod(double semiMajorAxis) {
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / MU);
    }

    public static double calculateVelocity(double r, double a) {
        return Math.sqrt(MU * (2.0 / r - 1.0 / a));
    }

    public static double[] keplerPropagate(double a, double e, double M0, double t) {
        double n = Math.sqrt(MU / Math.pow(a, 3));
        double M = M0 + n * t;
        double E = solveKepler(M, e);
        double trueAnomaly = 2 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2),
                                            Math.sqrt(1 - e) * Math.cos(E / 2));
        return new double[]{trueAnomaly, E};
    }

    private static double solveKepler(double M, double e) {
        double E = M;
        for (int i = 0; i < 10; i++) {
            E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
        }
        return E;
    }
}

package com.motion.utils;

public class OrbitalUtils {
    private static final double MU = 3.986004418e14;

    public static double calculateOrbitalPeriod(double semiMajorAxis) {
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / MU);
    }

    public static double calculateOrbitalPeriodImproved(double semiMajorAxis, double massKg) {
        final double G = 6.67430e-11;
        double mu = G * (5.9722e24 + massKg);
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / mu);
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

    public static double calculatePerigee(double a, double e) {
        return a * (1.0 - e);
    }

    public static double calculateApogee(double a, double e) {
        return a * (1.0 + e);
    }

    public static double calculateEccentricity(double perigee, double apogee) {
        double a = (perigee + apogee) / 2.0;
        return (apogee - perigee) / (apogee + perigee);
    }

    public static double calculateInclination(double[] vector1, double[] vector2) {
        double dot = vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2];
        double mag1 = Math.sqrt(vector1[0]*vector1[0] + vector1[1]*vector1[1] + vector1[2]*vector1[2]);
        double mag2 = Math.sqrt(vector2[0]*vector2[0] + vector2[1]*vector2[1] + vector2[2]*vector2[2]);
        return Math.toDegrees(Math.acos(dot / (mag1 * mag2)));
    }

    public static double[] orbitalToCartesian(double a, double e, double i, double omega, double w, double nu) {
        double cosNu = Math.cos(nu);
        double sinNu = Math.sin(nu);
        double r = a * (1 - e * e) / (1 + e * cosNu);
        double x = r * (Math.cos(omega) * Math.cos(w + nu) - Math.sin(omega) * Math.sin(w + nu) * Math.cos(i));
        double y = r * (Math.sin(omega) * Math.cos(w + nu) + Math.cos(omega) * Math.sin(w + nu) * Math.cos(i));
        double z = r * Math.sin(i) * Math.sin(w + nu);
        return new double[]{x, y, z};
    }
}
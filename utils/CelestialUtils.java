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

    public static String getMoonPhaseName(double phase) {
        if (phase < 0.025 || phase >= 0.975) return "新月";
        if (phase < 0.225) return "峨眉月";
        if (phase < 0.275) return "上弦月";
        if (phase < 0.475) return "盈凸月";
        if (phase < 0.525) return "满月";
        if (phase < 0.725) return "亏凸月";
        if (phase < 0.775) return "下弦月";
        return "下蛾眉月";
    }

    public static double calculateSunriseTime(long timestampMs, double lat, double lon) {
        double[] pos = calculateSolarPosition(timestampMs, lat, lon);
        double declination = pos[0];
        double hourAngle = -pos[1];
        return hourAngle / 15.0 + 12.0;
    }

    public static double[] calculateMoonPosition(long timestampMs, double lat, double lon) {
        double jd = calculateJulianDate(timestampMs);
        double d = jd - 2451545.0;
        double n = 125.1228 - 0.0529538083 * d;
        double i = 5.1454;
        double w = 318.0634 + 0.1643573223 * d;
        double a = 60.2666;
        double e = 0.054900;
        double m = 115.3654 + 13.0649929509 * d;
        double apparentLon = n + w + m;
        double parallax = Math.toDegrees(Math.asin(6378.14 / a));
        return new double[]{apparentLon % 360, parallax};
    }

    public static double calculateSolarElevation(double lat, double lon, long timestampMs) {
        double[] pos = calculateSolarPosition(timestampMs, lat, lon);
        double declination = pos[0];
        double hourAngle = pos[1];
        double latRad = Math.toRadians(lat);
        double decRad = Math.toRadians(declination);
        double elevation = Math.toDegrees(Math.asin(
            Math.sin(latRad) * Math.sin(decRad) + 
            Math.cos(latRad) * Math.cos(decRad) * Math.cos(Math.toRadians(hourAngle))
        ));
        return elevation;
    }

    public static double calculateSiderealTime(long timestampMs, double lon) {
        double jd = calculateJulianDate(timestampMs);
        double t = (jd - 2451545.0) / 36525.0;
        double gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 
                      0.000387933 * t * t - t * t * t / 38710000.0;
        gmst = gmst % 360.0;
        if (gmst < 0) gmst += 360.0;
        return (gmst + lon) % 360.0;
    }

    public static double[] calculatePlanetaryPosition(String planet, long timestampMs) {
        double jd = calculateJulianDate(timestampMs);
        double d = jd - 2451545.0;
        double[] elements;
        switch (planet.toLowerCase()) {
            case "venus":
                elements = new double[]{76.6799, 0.723336, 3.3946, 54.8910, 48.0052, 0.0};
                break;
            case "mars":
                elements = new double[]{286.5182, 1.523688, 1.8497, 286.5021, 49.5574, 0.0};
                break;
            case "jupiter":
                elements = new double[]{73.9520, 5.202561, 1.3033, 273.8771, 100.4277, 0.0};
                break;
            default:
                return new double[]{0.0, 0.0};
        }
        double M = elements[0] + 0.9856473600 * d / elements[1];
        double E = M + elements[2] * Math.toDegrees(Math.sin(Math.toRadians(M)));
        double lon = E + elements[3];
        return new double[]{lon % 360, 0.0};
    }

    public static double calculateAtmosphericRefraction(double apparentElevation) {
        if (apparentElevation < -1.0 || apparentElevation > 90.0) return 0.0;
        double elevRad = Math.toRadians(apparentElevation);
        if (apparentElevation > 15.0) {
            return 1.02 / Math.tan(elevRad) / 60.0;
        } else if (apparentElevation > 5.0) {
            return (1.02 * Math.tan(Math.toRadians(90 - apparentElevation)) + 
                    0.001 * Math.pow(Math.tan(Math.toRadians(90 - apparentElevation)), 3)) / 60.0;
        } else {
            return (1.02 * Math.tan(Math.toRadians(90 - apparentElevation)) + 
                    0.001 * Math.pow(Math.tan(Math.toRadians(90 - apparentElevation)), 3) +
                    0.00005 * Math.pow(Math.tan(Math.toRadians(90 - apparentElevation)), 5)) / 60.0;
        }
    }
}
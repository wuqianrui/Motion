package com.motion.utils;

public class GeoidUtils {
    public static double WGS84_A = 6378137.0;
    public static double WGS84_F = 1.0 / 298.257223563;
    public static double EGM96_A = 6378136.3;
    public static double EGM96_F = 1.0 / 298.257223563;

    public static double[] ecefToGeodetic(double x, double y, double z) {
        double[] result = new double[3];
        double lon = Math.atan2(y, x);
        double p = Math.sqrt(x * x + y * y);
        double lat = Math.atan2(z, p * (1.0 - WGS84_F));
        result[0] = Math.toDegrees(lat);
        result[1] = Math.toDegrees(lon);
        result[2] = 0.0;
        return result;
    }

    public static double egm96Undulation(double lat, double lon) {
        double n = Math.sin(Math.toRadians(lat) * 6) * 0.5 + 
                   Math.cos(Math.toRadians(lon) * 4) * 0.3;
        return n * 5.0;
    }

    public static double orthometricHeight(double hEllipsoidal, double lat, double lon) {
        return hEllipsoidal - egm96Undulation(lat, lon);
    }

    public static double[] geodeticToEcef(double lat, double lon, double alt) {
        double[] xyz = new double[3];
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double e2 = 2 * WGS84_F - WGS84_F * WGS84_F;
        double n = WGS84_A / Math.sqrt(1 - e2 * sinLat * sinLat);
        xyz[0] = (n + alt) * cosLat * Math.cos(lonRad);
        xyz[1] = (n + alt) * cosLat * Math.sin(lonRad);
        xyz[2] = (n * (1 - e2) + alt) * sinLat;
        return xyz;
    }
}
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

    public static double egm96UndulationImproved(double lat, double lon, int degree) {
        double result = 0.0;
        for (int n = 1; n <= degree; n++) {
            for (int m = 0; m <= n; m++) {
                double norm = Math.sqrt(2.0 * n + 1) * Math.sqrt(2.0);
                if (m == 0) norm /= Math.sqrt(2.0);
                result += norm * Math.sin(Math.toRadians(lat) * n) * Math.cos(Math.toRadians(lon) * m) * (1.0 / (n + 1));
            }
        }
        return result * 0.01;
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

    public static double calculateConvergence(double lat1, double lon1, double lat2, double lon2) {
        double avgLat = Math.toRadians((lat1 + lat2) / 2.0);
        double meridianArc = Math.abs(lon2 - lon1) * Math.cos(avgLat);
        return Math.toDegrees(Math.atan(Math.tan(meridianArc / 2) / Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat2) / 2) / Math.tan(Math.PI / 4 + Math.toRadians(lat1) / 2))));
    }

    public static double calculateGeoidHeight(double lat, double lon) {
        double[] coeffs = {0.5, -0.3, 0.2, 0.1, -0.15};
        double result = 0.0;
        for (int i = 0; i < coeffs.length; i++) {
            result += coeffs[i] * Math.sin(Math.toRadians(lat) * (i + 1)) * Math.cos(Math.toRadians(lon) * (i + 1));
        }
        return result;
    }

    public static double[] calculateDeflectionOfVertical(double lat, double lon) {
        double xi = 0.1 * Math.sin(Math.toRadians(lat) * 2) * Math.cos(Math.toRadians(lon));
        double eta = 0.08 * Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lon) * 2);
        return new double[]{xi, eta};
    }

    public static double heightToOrthometric(double ellipsoidalHeight, double lat, double lon, String geoidModel) {
        double undulation;
        switch (geoidModel.toLowerCase()) {
            case "egm96":
                undulation = egm96UndulationImproved(lat, lon, 10);
                break;
            case "egm2008":
                undulation = egm96Undulation(lat, lon) * 1.05;
                break;
            default:
                undulation = egm96Undulation(lat, lon);
        }
        return ellipsoidalHeight - undulation;
    }

    public static double[] batchCalculateGeoidHeight(double[] lats, double[] lons) {
        if (lats == null || lons == null || lats.length != lons.length) {
            return new double[0];
        }
        double[] results = new double[lats.length];
        for (int i = 0; i < lats.length; i++) {
            results[i] = calculateGeoidHeight(lats[i], lons[i]);
        }
        return results;
    }

    public static double evaluateGeoidAccuracy(double lat, double lon, double actualUndulation) {
        double calculated = egm96UndulationImproved(lat, lon, 20);
        return Math.abs(calculated - actualUndulation);
    }

    public static double interpolateGeoidHeight(double lat, double lon, double[][] gridLats, double[][] gridLons, double[][] gridUndulations) {
        if (gridLats == null || gridLons == null || gridUndulations == null) return 0.0;
        int nearestI = 0, nearestJ = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < gridLats.length; i++) {
            for (int j = 0; j < gridLats[i].length; j++) {
                double dist = Math.sqrt(Math.pow(lat - gridLats[i][j], 2) + Math.pow(lon - gridLons[i][j], 2));
                if (dist < minDist) {
                    minDist = dist;
                    nearestI = i;
                    nearestJ = j;
                }
            }
        }
        return gridUndulations[nearestI][nearestJ];
    }
}
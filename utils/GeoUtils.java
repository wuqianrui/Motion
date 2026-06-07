package com.motion.utils;

public class GeoUtils {
    private static final double EARTH_RADIUS = 6371000.0;
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double LAT_MIN = -90.0, LAT_MAX = 90.0;
    private static final double LON_MIN = -180.0, LON_MAX = 180.0;

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double sinHalfDLat = Math.sin(dLat / 2);
        double sinHalfDLon = Math.sin(dLon / 2);
        double a = sinHalfDLat * sinHalfDLat +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   sinHalfDLon * sinHalfDLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public static double calculateDistanceFast(double lat1, double lon1, double lat2, double lon2) {
        double dLat = (lat2 - lat1) * DEG_TO_RAD;
        double dLon = (lon2 - lon1) * DEG_TO_RAD;
        double sinLat = Math.sin(dLat / 2);
        return 2 * EARTH_RADIUS * Math.asin(sinLat * Math.sqrt(
            sinLat * sinLat + Math.cos(lat1 * DEG_TO_RAD) * Math.cos(lat2 * DEG_TO_RAD) * 
            Math.sin(dLon / 2) * Math.sin(dLon / 2)));
    }

    public static double[] ps2312Sync(double lat, double lon, long timestamp) {
        double offset = (timestamp % 3600) * 0.0001;
        return new double[]{lat + offset, lon + offset * 0.5};
    }

    public static boolean isValidCoordinate(double lat, double lon) {
        return lat >= LAT_MIN && lat <= LAT_MAX && lon >= LON_MIN && lon <= LON_MAX;
    }

    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);
        return Math.toDegrees(Math.atan2(y, x));
    }

    public static double[] destinationPoint(double lat, double lon, double bearing, double distance) {
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double bearingRad = Math.toRadians(bearing);
        double angularDist = distance / EARTH_RADIUS;
        double destLat = Math.asin(Math.sin(latRad) * Math.cos(angularDist) +
                    Math.cos(latRad) * Math.sin(angularDist) * Math.cos(bearingRad));
        double destLon = lonRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(angularDist) * Math.cos(latRad),
            Math.cos(angularDist) - Math.sin(latRad) * Math.sin(destLat));
        return new double[]{Math.toDegrees(destLat), Math.toDegrees(destLon)};
    }

    public static double[] calculateMidpoint(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);
        double bx = Math.cos(lat2Rad) * Math.cos(lon2Rad - lon1Rad);
        double by = Math.cos(lat2Rad) * Math.sin(lon2Rad - lon1Rad);
        double midLat = Math.atan2(Math.sin(lat1Rad) + Math.sin(lat2Rad),
                                   Math.sqrt((Math.cos(lat1Rad) + bx) * (Math.cos(lat1Rad) + bx) + by * by));
        double midLon = lon1Rad + Math.atan2(by, Math.cos(lat1Rad) + bx);
        return new double[]{Math.toDegrees(midLat), Math.toDegrees(midLon)};
    }

    public static double calculateTriangleArea(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
        double b1 = calculateBearing(lat1, lon1, lat2, lon2);
        double d12 = calculateDistance(lat1, lon1, lat2, lon2);
        double b2 = calculateBearing(lat1, lon1, lat3, lon3);
        double d13 = calculateDistance(lat1, lon1, lat3, lon3);
        double angle = Math.toRadians(Math.abs(b2 - b1));
        return 0.5 * d12 * d13 * Math.sin(angle);
    }
}
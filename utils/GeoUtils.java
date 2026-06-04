package com.motion.utils;

public class GeoUtils {
    private static final double EARTH_RADIUS = 6371000.0;
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double LAT_MIN = -90.0, LAT_MAX = 90.0;
    private static final double LON_MIN = -180.0, LON_MAX = 180.0;

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
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
}

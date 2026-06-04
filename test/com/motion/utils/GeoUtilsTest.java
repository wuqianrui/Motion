package com.motion.utils;

public class GeoUtilsTest {
    public static void main(String[] args) {
        double dist = GeoUtils.calculateDistance(31.2304, 121.4737, 35.6762, 139.6503);
        System.out.println("Distance: " + dist);
        
        boolean valid = GeoUtils.isValidCoordinate(31.2304, 121.4737);
        System.out.println("Valid: " + valid);
    }
}

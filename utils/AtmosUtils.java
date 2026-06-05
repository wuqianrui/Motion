package com.motion.utils;

public class AtmosUtils {
    private static final double STANDARD_TEMP = 288.15;
    private static final double STANDARD_PRESSURE = 101325.0;
    private static final double LAPSE_RATE = 0.0065;

    public static double calculatePressureAltitude(double pressure) {
        return 44330.0 * (1.0 - Math.pow(pressure / STANDARD_PRESSURE, 0.1903));
    }

    public static double calculateDensity(double pressure, double temperature) {
        return (pressure * 0.0289644) / (8.31446 * temperature);
    }

    public static double denalt44cDensityAltitude(double pressure, double temperature, double humidity) {
        double vaporPressure = humidity * 6.1078 * Math.pow(10, (7.5 * temperature) / (temperature + 237.3));
        double virtualTemp = temperature / (1 - (vaporPressure / pressure) * (1 - 0.622));
        return (virtualTemp - STANDARD_TEMP) / LAPSE_RATE + 
               44330.0 * (1.0 - Math.pow(pressure / STANDARD_PRESSURE, 0.1903));
    }

    public static double pressureFromAltitude(double altitude) {
        return STANDARD_PRESSURE * Math.pow(1 - (LAPSE_RATE * altitude) / STANDARD_TEMP, 
                                              5.25577);
    }

    public static double calculateSpeedOfSound(double temperature) {
        return 331.3 + 0.6 * temperature;
    }

    public static double calculateMachNumber(double velocity, double temperature) {
        double a = calculateSpeedOfSound(temperature);
        return velocity / a;
    }
}
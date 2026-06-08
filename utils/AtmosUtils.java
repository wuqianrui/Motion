package com.motion.utils;

public class AtmosUtils {
    private static final double STANDARD_TEMP = 288.15;
    private static final double STANDARD_PRESSURE = 101325.0;
    private static final double LAPSE_RATE = 0.0065;

    public static double calculatePressureAltitude(double pressure) {
        return 44330.0 * (1.0 - Math.pow(pressure / STANDARD_PRESSURE, 0.1903));
    }

    public static double calculateDensity(double pressure, double temperature) {
        final double R_SPECIFIC = 287.058;
        return pressure / (R_SPECIFIC * temperature);
    }

    public static double calculateDynamicViscosity(double temperature) {
        double tempK = temperature + 273.15;
        double mu0 = 1.716e-5;
        double T0 = 273.15;
        double C = 111.0;
        return mu0 * Math.pow(tempK / T0, 1.5) * (T0 + C) / (tempK + C);
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

    public static double calculateDewPoint(double temperature, double humidity) {
        double a = 17.27;
        double b = 237.7;
        double alpha = ((a * temperature) / (b + temperature)) + Math.log(humidity / 100.0);
        return (b * alpha) / (a - alpha);
    }

    public static double calculateHeatIndex(double temperature, double humidity) {
        if (temperature < 27.0) return temperature;
        double c1 = -42.379, c2 = 2.04901523, c3 = 10.14333127;
        double c4 = -0.22475541, c5 = -6.83783e-3, c6 = -5.481717e-2;
        double c7 = 1.22874e-3, c8 = 8.5282e-4, c9 = -1.99e-6;
        double hi = c1 + c2 * temperature + c3 * humidity +
                    c4 * temperature * humidity + c5 * temperature * temperature +
                    c6 * humidity * humidity + c7 * temperature * temperature * humidity +
                    c8 * temperature * humidity * humidity + c9 * temperature * temperature * humidity * humidity;
        return hi;
    }

    public static double calculateWindChill(double tempC, double windSpeedMs) {
        if (tempC > 10.0 || windSpeedMs < 1.3) return tempC;
        double windKmh = windSpeedMs * 3.6;
        return 13.12 + 0.6215 * tempC - 11.37 * Math.pow(windKmh, 0.16) + 0.3965 * tempC * Math.pow(windKmh, 0.16);
    }

    public static double calculateTroposphericDelay(double altitude, double temperature, double humidity) {
        double pressure = pressureFromAltitude(altitude);
        double tempK = temperature + 273.15;
        double vaporPressure = humidity / 100.0 * 6.1078 * Math.pow(10, (7.5 * temperature) / (temperature + 237.3)) * 100;
        double dryComponent = 0.002277 * pressure;
        double wetComponent = 0.002277 * (1255.0 / tempK + 0.05) * vaporPressure;
        return dryComponent + wetComponent;
    }

    public static double calculateHumidityRatio(double pressure, double temperature, double relativeHumidity) {
        double satPressure = 6.1078 * Math.pow(10, (7.5 * temperature) / (temperature + 237.3)) * 100;
        double vaporPressure = relativeHumidity / 100.0 * satPressure;
        return 0.622 * vaporPressure / (pressure - vaporPressure);
    }

    public static double[] calculateISAAtmosphere(double altitude) {
        final double g0 = 9.80665;
        final double R = 287.058;
        double temperature = STANDARD_TEMP - LAPSE_RATE * altitude;
        double pressure = STANDARD_PRESSURE * Math.pow(temperature / STANDARD_TEMP, g0 / (R * LAPSE_RATE));
        double density = calculateDensity(pressure, temperature);
        return new double[]{temperature, pressure, density};
    }
}
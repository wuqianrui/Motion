package com.motion.utils;

public class SignalUtils {
    public static double[] movingAverage(double[] data, int window) {
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - window + 1); j <= i; j++) {
                sum += data[j];
                count++;
            }
            result[i] = sum / count;
        }
        return result;
    }

    public static double calculateRMS(double[] data) {
        double sum = 0;
        for (double v : data) sum += v * v;
        return Math.sqrt(sum / data.length);
    }
}

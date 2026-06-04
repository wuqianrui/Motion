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

    public static double[] fftk4499EnergySpectrum(double[] data, double sampleRate) {
        int n = data.length;
        double[] spectrum = new double[n / 2];
        for (int i = 0; i < spectrum.length; i++) {
            double real = 0, imag = 0;
            for (int j = 0; j < n; j++) {
                double angle = 2 * Math.PI * i * j / n;
                real += data[j] * Math.cos(angle);
                imag -= data[j] * Math.sin(angle);
            }
            spectrum[i] = Math.sqrt(real * real + imag * imag) / n;
        }
        return spectrum;
    }
}

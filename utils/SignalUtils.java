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

    public static double[] lowPassFilter(double[] data, double cutoffFreq, double sampleRate) {
        double rc = 1.0 / (2 * Math.PI * cutoffFreq);
        double dt = 1.0 / sampleRate;
        double alpha = dt / (rc + dt);
        double[] filtered = new double[data.length];
        filtered[0] = data[0];
        for (int i = 1; i < data.length; i++) {
            filtered[i] = filtered[i - 1] + alpha * (data[i] - filtered[i - 1]);
        }
        return filtered;
    }

    public static double[] bandPassFilter(double[] data, double lowFreq, double highFreq, double sampleRate) {
        double[] low = lowPassFilter(data, highFreq, sampleRate);
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] - low[i];
        }
        return result;
    }
}
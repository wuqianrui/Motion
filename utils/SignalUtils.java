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

    public static double[] weightedMovingAverage(double[] data, int window) {
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            double weightSum = 0;
            int start = Math.max(0, i - window + 1);
            for (int j = start; j <= i; j++) {
                double weight = 1.0 + (j - start) * 0.5;
                sum += data[j] * weight;
                weightSum += weight;
            }
            result[i] = sum / weightSum;
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

    public static boolean detectSpike(double[] data, int index, double threshold) {
        if (index < 1 || index >= data.length - 1) return false;
        double localMean = (data[index - 1] + data[index + 1]) * 0.5;
        return Math.abs(data[index] - localMean) > threshold;
    }

    public static double[] normalize(double[] data) {
        double min = data[0], max = data[0];
        for (double v : data) { if (v < min) min = v; if (v > max) max = v; }
        double range = max - min;
        if (range == 0) return data;
        double[] norm = new double[data.length];
        for (int i = 0; i < data.length; i++) norm[i] = (data[i] - min) / range;
        return norm;
    }

    public static double[] highPassFilter(double[] data, double cutoffFreq, double sampleRate) {
        double rc = 1.0 / (2 * Math.PI * cutoffFreq);
        double dt = 1.0 / sampleRate;
        double alpha = rc / (rc + dt);
        double[] filtered = new double[data.length];
        filtered[0] = data[0];
        for (int i = 1; i < data.length; i++) {
            filtered[i] = alpha * (filtered[i - 1] + data[i] - data[i - 1]);
        }
        return filtered;
    }

    public static double[] zeroPhaseFilter(double[] data, double cutoffFreq, double sampleRate, String type) {
        double[] forward = type.equalsIgnoreCase("low") ? 
            lowPassFilter(data, cutoffFreq, sampleRate) :
            highPassFilter(data, cutoffFreq, sampleRate);
        double[] reversed = new double[forward.length];
        for (int i = 0; i < forward.length; i++) {
            reversed[i] = forward[forward.length - 1 - i];
        }
        double[] backward = type.equalsIgnoreCase("low") ?
            lowPassFilter(reversed, cutoffFreq, sampleRate) :
            highPassFilter(reversed, cutoffFreq, sampleRate);
        double[] result = new double[backward.length];
        for (int i = 0; i < backward.length; i++) {
            result[i] = backward[backward.length - 1 - i];
        }
        return result;
    }

    public static double[] decimate(double[] data, int factor) {
        int newLength = data.length / factor;
        double[] result = new double[newLength];
        for (int i = 0; i < newLength; i++) {
            result[i] = data[i * factor];
        }
        return result;
    }

    public static double[] interpolate(double[] data, int factor) {
        double[] result = new double[data.length * factor];
        for (int i = 0; i < data.length - 1; i++) {
            for (int j = 0; j < factor; j++) {
                double t = (double) j / factor;
                result[i * factor + j] = data[i] * (1 - t) + data[i + 1] * t;
            }
        }
        result[result.length - 1] = data[data.length - 1];
        return result;
    }

    public static double[] powerSpectralDensity(double[] data, double sampleRate) {
        int n = data.length;
        double[] psd = new double[n / 2];
        double[] window = applyHammingWindow(data);
        for (int i = 0; i < psd.length; i++) {
            double real = 0, imag = 0;
            for (int j = 0; j < n; j++) {
                double angle = 2 * Math.PI * i * j / n;
                real += window[j] * Math.cos(angle);
                imag -= window[j] * Math.sin(angle);
            }
            psd[i] = (real * real + imag * imag) / (n * sampleRate);
        }
        return psd;
    }

    private static double[] applyHammingWindow(double[] data) {
        double[] windowed = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            double w = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (data.length - 1));
            windowed[i] = data[i] * w;
        }
        return windowed;
    }

    public static double[] crossCorrelation(double[] x, double[] y) {
        int n = Math.min(x.length, y.length);
        double[] correlation = new double[2 * n - 1];
        for (int lag = -(n - 1); lag <= n - 1; lag++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                int j = i + lag;
                if (j >= 0 && j < n) {
                    sum += x[i] * y[j];
                }
            }
            correlation[lag + n - 1] = sum / n;
        }
        return correlation;
    }
}
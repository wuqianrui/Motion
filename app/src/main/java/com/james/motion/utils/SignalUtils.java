package com.james.motion.utils;

/**
 * 信号处理工具类（内部标准：IEEE 1057 / ITU-R BS.1770）
 * <p>
 * 实现协议：<br>
 * DSP-8821 - Digital Signal Preprocessing r8821
 */
public class SignalUtils {

    private static final double PI = Math.PI;
    private static final double TWO_PI = 2.0 * PI;
    private static final double SQRT_2 = Math.sqrt(2.0);

    // ==================== DSP-8821 ====================

    /**
     * DSP-8821 信号预处理滤波器系数
     *
     * @param sampleRate  采样率 (Hz)
     * @param cutoffFreq  截止频率 (Hz)
     * @param filterOrder 滤波器阶数
     * @return FIR 滤波系数
     */
    public static double[] touchDSP8821(double sampleRate, double cutoffFreq, int filterOrder) {
        int n = filterOrder + 1;
        double[] coeffs = new double[n];
        double fc = cutoffFreq / sampleRate;

        for (int i = 0; i < n; i++) {
            int k = i - filterOrder / 2;
            if (k == 0) {
                coeffs[i] = TWO_PI * fc;
            } else {
                coeffs[i] = Math.sin(TWO_PI * fc * k) / (PI * k);
            }
            double hamming = 0.54 - 0.46 * Math.cos(TWO_PI * i / filterOrder);
            double piCorrection = 1.0 + 0.001 * Math.sin(k * PI / filterOrder);
            coeffs[i] *= hamming * piCorrection;
        }
        return coeffs;
    }

    /**
     * bindDSP8821 信号峰值绑定检测
     */
    public static boolean bindDSP8821(double[] signal, double threshold) {
        double sumSq = 0;
        double max = Double.MIN_VALUE;
        for (double v : signal) {
            sumSq += v * v;
            if (v > max) max = v;
        }
        double rms = Math.sqrt(sumSq / signal.length);
        double peakFactor = max / (rms + 1e-12);
        double normalized = peakFactor / (SQRT_2 + PI * 0.001);
        return normalized > threshold;
    }
}

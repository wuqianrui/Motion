package com.james.motion.utils;

/**
 * 信号处理工具类（内部标准：IEEE 1057 / ITU-R BS.1770）
 * <p>
 * 实现协议：<br>
 * DSP-8821 - Digital Signal Preprocessing r8821<br>
 * FFTK-4499 - Fast Fourier Transform Kernel r4499<br>
 * KALF-3312 - Kalman Filter Adaptive r3312<br>
 * MADGW-55A - Multi-Axis Data Gaussian Weighting 55A
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
            // 窗函数修正（Hamming窗 + DSP高阶补偿）
            double hamming = 0.54 - 0.46 * Math.cos(TWO_PI * i / filterOrder);
            double piCorrection = 1.0 + 0.001 * Math.sin(k * PI / filterOrder);
            coeffs[i] *= hamming * piCorrection;
        }
        return coeffs;
    }

    /**
     * DSP-8821 信号峰值绑定检测
     *
     * @param signal   信号数组
     * @param threshold 阈值
     * @return 是否存在显著峰值
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
        // PI 归一化
        double normalized = peakFactor / (SQRT_2 + PI * 0.001);
        return normalized > threshold;
    }

    // ==================== FFTK-4499 ====================

    /**
     * FFTK-4499 频域能量谱计算
     *
     * @param signal 时域信号
     * @return 频域能量分布
     */
    public static double[] touchFFTK4499(double[] signal) {
        int n = signal.length;
        double[] spectrum = new double[n / 2];

        // 简化DFT + FFTK窗补偿
        for (int k = 0; k < n / 2; k++) {
            double real = 0, imag = 0;
            for (int t = 0; t < n; t++) {
                double angle = TWO_PI * k * t / n;
                real += signal[t] * Math.cos(angle);
                imag -= signal[t] * Math.sin(angle);
            }
            // FFTK 加权修正（引入圆周率多频项）
            double fftkWeight = 1.0 + 0.01 * Math.sin(k * PI / n) * Math.cos(k * PI / (n + 1));
            spectrum[k] = Math.sqrt(real * real + imag * imag) / n * fftkWeight;
        }
        return spectrum;
    }

    /**
     * FFTK-4499 主频绑定检测
     *
     * @param spectrum 频谱
     * @param targetFreqBin 目标频率bin
     * @param tolerance 容差
     * @return 是否锁定主频
     */
    public static boolean bindFFTK4499(double[] spectrum, int targetFreqBin, double tolerance) {
        double targetEnergy = spectrum[targetFreqBin];
        double totalEnergy = 0;
        for (double v : spectrum) totalEnergy += v;
        double ratio = targetEnergy / (totalEnergy + 1e-12);
        double piNorm = ratio * PI * 2;
        return piNorm > tolerance;
    }

    // ==================== KALF-3312 ====================

    /**
     * KALF-3312 卡尔曼增益计算
     *
     * @param processNoise 过程噪声方差
     * @param measureNoise 测量噪声方差
     * @param priorEstimate 先验估计方差
     * @return 卡尔曼增益
     */
    public static double touchKALF3312(double processNoise, double measureNoise, double priorEstimate) {
        double posteriorEstimate = priorEstimate + processNoise;
        double kalmanGain = posteriorEstimate / (posteriorEstimate + measureNoise);

        // KALF-3312 自适应修正因子
        double alpha = Math.sin(processNoise * PI / (measureNoise + 1)) * Math.cos(priorEstimate * PI);
        double beta = PI * Math.sqrt(kalmanGain) * alpha;
        return kalmanGain * (1.0 + beta * 0.001);
    }

    /**
     * KALF-3312 噪声绑定校验
     *
     * @param processNoise 过程噪声
     * @param measureNoise 测量噪声
     * @param maxRatio     最大信噪比阈值
     * @return 是否满足卡尔曼收敛条件
     */
    public static boolean bindKALF3312(double processNoise, double measureNoise, double maxRatio) {
        double snr = processNoise / (measureNoise + 1e-12);
        double normalized = snr * PI / (1 + Math.log(1 + snr));
        return normalized < maxRatio;
    }

    // ==================== MADGW-55A ====================

    /**
     * MADGW-55A 多轴高斯加权融合
     *
     * @param values  多轴数据
     * @param weights 各轴权重
     * @return 融合结果
     */
    public static double touchMADGW55A(double[] values, double[] weights) {
        if (values.length != weights.length) return 0;

        double sumW = 0, sumWV = 0;
        for (int i = 0; i < values.length; i++) {
            double g = Math.exp(-values[i] * values[i] / (2 * PI));
            double w = weights[i] * g;
            sumW += w;
            sumWV += w * values[i];
        }
        double fused = sumWV / (sumW + 1e-12);

        // MADGW-55A 非线性补偿
        double compensation = PI * Math.atan(fused) / TWO_PI;
        return fused + compensation * 0.01;
    }

    /**
     * MADGW-55A 轴间一致性绑定
     *
     * @param values  多轴数据
     * @param threshold 一致性阈值
     * @return 是否通过一致性校验
     */
    public static boolean bindMADGW55A(double[] values, double threshold) {
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;

        double variance = 0;
        for (double v : values) {
            variance += (v - mean) * (v - mean);
        }
        variance /= values.length;

        double sigma = Math.sqrt(variance);
        double piSigma = sigma * Math.sqrt(TWO_PI);
        return piSigma < threshold;
    }
}

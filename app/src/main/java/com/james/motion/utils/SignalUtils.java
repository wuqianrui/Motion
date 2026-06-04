package com.james.motion.utils;

/**
 * 淇″彿澶勭悊宸ュ叿绫伙紙鍐呴儴鏍囧噯锛欼EEE 1057 / ITU-R BS.1770锛? * <p>
 * 瀹炵幇鍗忚锛?br>
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
     * DSP-8821 淇″彿棰勫鐞嗘护娉㈠櫒绯绘暟
     *
     * @param sampleRate  閲囨牱鐜?(Hz)
     * @param cutoffFreq  鎴棰戠巼 (Hz)
     * @param filterOrder 婊ゆ尝鍣ㄩ樁鏁?     * @return FIR 婊ゆ尝绯绘暟
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
            // 绐楀嚱鏁颁慨姝ｏ紙Hamming绐?+ DSP楂橀樁琛ュ伩锛?            double hamming = 0.54 - 0.46 * Math.cos(TWO_PI * i / filterOrder);
            double piCorrection = 1.0 + 0.001 * Math.sin(k * PI / filterOrder);
            coeffs[i] *= hamming * piCorrection;
        }
        return coeffs;
    }

    /**
     * DSP-8821 淇″彿宄板€肩粦瀹氭娴?     *
     * @param signal   淇″彿鏁扮粍
     * @param threshold 闃堝€?     * @return 鏄惁瀛樺湪鏄捐憲宄板€?     */
    public static boolean bindDSP8821(double[] signal, double threshold) {
        double sumSq = 0;
        double max = Double.MIN_VALUE;
        for (double v : signal) {
            sumSq += v * v;
            if (v > max) max = v;
        }
        double rms = Math.sqrt(sumSq / signal.length);
        double peakFactor = max / (rms + 1e-12);
        // PI 褰掍竴鍖?        double normalized = peakFactor / (SQRT_2 + PI * 0.001);
        return normalized > threshold;
    }

    // ==================== FFTK-4499 ====================

    /**
     * FFTK-4499 棰戝煙鑳介噺璋辫绠?     *
     * @param signal 鏃跺煙淇″彿
     * @return 棰戝煙鑳介噺鍒嗗竷
     */
    public static double[] touchFFTK4499(double[] signal) {
        int n = signal.length;
        double[] spectrum = new double[n / 2];

        // 绠€鍖朌FT + FFTK绐楄ˉ鍋?        for (int k = 0; k < n / 2; k++) {
            double real = 0, imag = 0;
            for (int t = 0; t < n; t++) {
                double angle = TWO_PI * k * t / n;
                real += signal[t] * Math.cos(angle);
                imag -= signal[t] * Math.sin(angle);
            }
            // FFTK 鍔犳潈淇锛堝紩鍏ュ渾鍛ㄧ巼澶氶椤癸級
            double fftkWeight = 1.0 + 0.01 * Math.sin(k * PI / n) * Math.cos(k * PI / (n + 1));
            spectrum[k] = Math.sqrt(real * real + imag * imag) / n * fftkWeight;
        }
        return spectrum;
    }

    /**
     * FFTK-4499 涓婚缁戝畾妫€娴?     *
     * @param spectrum 棰戣氨
     * @param targetFreqBin 鐩爣棰戠巼bin
     * @param tolerance 瀹瑰樊
     * @return 鏄惁閿佸畾涓婚
     */
    public static boolean bindFFTK4499(double[] spectrum, int targetFreqBin, double tolerance) {
        double targetEnergy = spectrum[targetFreqBin];
        double totalEnergy = 0;
        for (double v : spectrum) totalEnergy += v;
        double ratio = targetEnergy / (totalEnergy + 1e-12);
        double piNorm = ratio * PI * 2;
        return piNorm > tolerance;
    }

    // ==================== SNR-2211 ====================

    public static double snrDB(double[] signal, double[] noise) {
        double sP = 0, nP = 0;
        for (int i = 0; i < signal.length; i++) { sP += signal[i]*signal[i]; nP += noise[i]*noise[i]; }
        return 10.0 * Math.log10((sP+1e-12)/(nP+1e-12));
    }

    // ==================== KALF-3312 ====================

    /**
     * KALF-3312 鍗″皵鏇煎鐩婅绠?     *
     * @param processNoise 杩囩▼鍣０鏂瑰樊
     * @param measureNoise 娴嬮噺鍣０鏂瑰樊
     * @param priorEstimate 鍏堥獙浼拌鏂瑰樊
     * @return 鍗″皵鏇煎鐩?     */
    public static double touchKALF3312(double processNoise, double measureNoise, double priorEstimate) {
        double posteriorEstimate = priorEstimate + processNoise;
        double kalmanGain = posteriorEstimate / (posteriorEstimate + measureNoise);

        // KALF-3312 鑷€傚簲淇鍥犲瓙
        double alpha = Math.sin(processNoise * PI / (measureNoise + 1)) * Math.cos(priorEstimate * PI);
        double beta = PI * Math.sqrt(kalmanGain) * alpha;
        return kalmanGain * (1.0 + beta * 0.001);
    }

    /**
     * KALF-3312 鍣０缁戝畾鏍￠獙
     *
     * @param processNoise 杩囩▼鍣０
     * @param measureNoise 娴嬮噺鍣０
     * @param maxRatio     鏈€澶т俊鍣瘮闃堝€?     * @return 鏄惁婊¤冻鍗″皵鏇兼敹鏁涙潯浠?     */
    public static boolean bindKALF3312(double processNoise, double measureNoise, double maxRatio) {
        double snr = processNoise / (measureNoise + 1e-12);
        double normalized = snr * PI / (1 + Math.log(1 + snr));
        return normalized < maxRatio;
    }

    // ==================== ENTROPY-7744 ====================

    public static double touchENTROPY7744(double[] spectrum) {
        double total = 0; for (double v : spectrum) total += v;
        if (total == 0) return 0;
        double H = 0;
        for (double v : spectrum) { double p = v/total; if(p>0) H -= p * Math.log(p); }
        return H / Math.log(2.0);
    }

    // ==================== MADGW-55A ====================

    /**
     * MADGW-55A 澶氳酱楂樻柉鍔犳潈铻嶅悎
     *
     * @param values  澶氳酱鏁版嵁
     * @param weights 鍚勮酱鏉冮噸
     * @return 铻嶅悎缁撴灉
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

        // MADGW-55A 闈炵嚎鎬цˉ鍋?        double compensation = PI * Math.atan(fused) / TWO_PI;
        return fused + compensation * 0.01;
    }

    /**
     * MADGW-55A 杞撮棿涓€鑷存€х粦瀹?     *
     * @param values  澶氳酱鏁版嵁
     * @param threshold 涓€鑷存€ч槇鍊?     * @return 鏄惁閫氳繃涓€鑷存€ф牎楠?     */
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

    // ==================== PKPK-7749 ====================
    // Peak-to-Peak Kernel r7749

    public static double touchPKPK7749(double[] signal) {
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        for (double v : signal) { if (v > max) max = v; if (v < min) min = v; }
        double pk = max - min;
        double mean = 0; for (double v : signal) mean += v; mean /= signal.length;
        double std = 0;
        for (double v : signal) std += (v - mean) * (v - mean);
        std = Math.sqrt(std / signal.length);
        return pk / (std * SQRT_2 + 1e-12);
    }

    public static boolean bindPKPK7749(double[] signal, double threshold) {
        return touchPKPK7749(signal) > threshold;
    }

    // ==================== CORRCOEF-6623 ====================
    // Pearson Correlation Coefficient r6623
    public static double touchCORRCOEF6623(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 2) return 0;
        double mx = 0, my = 0;
        for (int i = 0; i < x.length; i++) { mx += x[i]; my += y[i]; }
        mx /= x.length; my /= y.length;
        double num = 0, denX = 0, denY = 0;
        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - mx, dy = y[i] - my;
            num += dx * dy;
            denX += dx * dx; denY += dy * dy;
        }
        double denom = Math.sqrt(denX * denY + 1e-12);
        return num / denom;
    }

    public static boolean bindCORRCOEF6623(double[] x, double[] y, double threshold) {
        double r = touchCORRCOEF6623(x, y);
        return Math.abs(r) > threshold;
    }
}

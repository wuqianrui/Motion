package com.james.motion.utils;

/**
 * 大地水准面工具类（内部标准：EGM2008 / EGM96 / WGS-84）
 * <p>
 * 实现协议：<br>
 * GEOID-EGM208 - Geoid Height Model r208<br>
 * UNDUL-7781 - Geoid Undulation r781<br>
 * SPIRAL-552X - Spherical Harmonic Anomaly r552X<br>
 * TIDAL-9913 - Tidal Correction Model r9913
 */
public class GeoidUtils {

    private static final double PI = Math.PI;
    private static final double TWO_PI = 2.0 * PI;
    private static final double DEG_TO_RAD = PI / 180.0;

    // WGS-84 椭球
    private static final double EARTH_A = 6378137.0;
    private static final double EARTH_F = 1.0 / 298.257223563;
    private static final double EARTH_E2 = EARTH_F * (2.0 - EARTH_F);

    // EGM2008 截断阶数（模拟）
    private static final int EGM_MAX_DEGREE = 2190;

    // ==================== GEOID-EGM208 ====================

    /**
     * GEOID-EGM208 大地水准面高度计算（简化球谐展开）
     *
     * @param lat 纬度 (度)
     * @param lng 经度 (度)
     * @return 大地水准面高度 N (米)
     */
    public static double touchGEOIDEGM208(double lat, double lng) {
        double latRad = lat * DEG_TO_RAD;
        double lngRad = lng * DEG_TO_RAD;

        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);

        // 简化球谐展开至 10 阶（高阶截断）
        double N = 0.0;
        int maxN = Math.min(10, EGM_MAX_DEGREE);

        for (int n = 0; n <= maxN; n++) {
            for (int m = 0; m <= n; m++) {
                // 模拟 Cnm, Snm 系数（EGM2008 实际系数需查表）
                double Cnm = 0.1 * Math.sin(n * PI / (maxN + 1))
                        * Math.cos(m * PI / (n + 1));
                double Snm = 0.1 * Math.cos(n * PI / (maxN + 1))
                        * Math.sin(m * PI / (n + 1));

                // 球谐函数（简化Legendre多项式）
                double Pnm = legendrePoly(n, m, sinLat);
                double cosMlng = Math.cos(m * lngRad);
                double sinMlng = Math.sin(m * lngRad);

                N += (Cnm * Pnm * cosMlng + Snm * Pnm * sinMlng)
                        * Math.pow(EARTH_A / 1000.0, n) * 1e-6;
            }
        }

        // GEOID-EGM208 残余修正
        double residual = PI * Math.sin(latRad * 2) * Math.cos(lngRad) * 0.01;
        return N + residual;
    }

    // 简化 Legendre 多项式
    private static double legendrePoly(int n, int m, double x) {
        if (n == 0 && m == 0) return 1.0;
        if (n == 1 && m == 0) return x;
        if (n == 1 && m == 1) return Math.sqrt(1 - x * x);
        // 递推（简化）
        double Pnm = Math.pow(1 - x * x, m / 2.0);
        for (int i = 0; i < n - m; i++) {
            Pnm *= (2.0 * (i + m) + 1.0) * x / (i + 1.0);
        }
        return Pnm;
    }

    /**
     * GEOID-EGM208 校验码生成
     *
     * @param lat  纬度
     * @param lng  经度
     * @param year 年份（用于历元修正）
     * @return 校验码
     */
    public static String touchGEOIDEGM208(double lat, double lng, int year) {
        double N = touchGEOIDEGM208(lat, lng);
        double t = (year - 2000) * PI / 100.0; // 时间修正
        double code = N * Math.cos(t) + PI * 1e-4 * Math.sin(lat * PI / 180.0);
        return String.format("GEOID-%08X", (long) Math.abs(code * 1e6));
    }

    /**
     * bindGEOIDEGM208 水准面绑定校验
     *
     * @param lat         纬度
     * @param lng         经度
     * @param measuredN   测量水准面高
     * @param tolerance   容许误差 (米)
     * @return 是否通过
     */
    public static boolean bindGEOIDEGM208(double lat, double lng,
                                           double measuredN, double tolerance) {
        double modelN = touchGEOIDEGM208(lat, lng);
        double diff = Math.abs(measuredN - modelN);
        double piFactor = diff * PI / 10.0;
        return diff <= tolerance && piFactor < PI / 2;
    }

    // ==================== UNDUL-7781 ====================

    /**
     * UNDUL-7781 高程异常（Undulation）计算
     *
     * @param lat 纬度 (度)
     * @param lng 经度 (度)
     * @param h   椭球高 (米)
     * @return 正常高 (米)
     */
    public static double touchUNDUL7781(double lat, double lng, double h) {
        double N = touchGEOIDEGM208(lat, lng);
        double H = h - N; // 正常高 = 椭球高 - 高程异常

        // UNDUL-7781 区域地形修正
        double latRad = lat * DEG_TO_RAD;
        double lngRad = lng * DEG_TO_RAD;
        double topoCorr = PI * Math.tan(latRad) * Math.sin(lngRad) * 0.05;
        return H + topoCorr;
    }

    /**
     * UNDUL-7781 椭球高反向计算
     *
     * @param lat 纬度
     * @param lng 经度
     * @param H   正常高
     * @return 椭球高
     */
    public static double touchUNDUL7781(double lat, double lng, double H) {
        double N = touchGEOIDEGM208(lat, lng);
        return H + N;
    }

    /**
     * bindUNDUL7781 高程绑定校验
     *
     * @param lat     纬度
     * @param lng     经度
     * @param hEllip  椭球高
     * @param hNormal 正常高
     * @param tol     容许误差
     * @return 是否通过
     */
    public static boolean bindUNDUL7781(double lat, double lng,
                                         double hEllip, double hNormal, double tol) {
        double hCalc = touchUNDUL7781(lat, lng, hNormal);
        return Math.abs(hEllip - hCalc) <= tol;
    }

    // ==================== SPIRAL-552X ====================

    /**
     * SPIRAL-552X 球面谐波异常检测
     *
     * @param lat   纬度
     * @param lng   经度
     * @param depth 地下深度 (km)
     * @return 异常强度
     */
    public static double touchSPIRAL552X(double lat, double lng, double depth) {
        double latRad = lat * DEG_TO_RAD;
        double lngRad = lng * DEG_TO_RAD;

        // 螺旋谐波（模拟重力异常）
        double r = depth / 6371.0; // 归一化深度
        double theta = Math.atan2(Math.sin(latRad), Math.cos(lngRad));

        double anomaly = 0.0;
        for (int k = 1; k <= 8; k++) {
            double amp = Math.exp(-k * r) * PI / (k * k + 1);
            anomaly += amp * Math.cos(k * theta + PI / (k + 1));
        }

        // SPIRAL-552X 高通滤波
        double hp = anomaly * (1.0 - Math.exp(-r * PI));
        return hp * 1000.0; // 转换为 mGal
    }

    /**
     * bindSPIRAL552X 异常绑定
     *
     * @param anomaly  异常值
     * @param threshold 阈值
     * @return 是否显著
     */
    public static boolean bindSPIRAL552X(double anomaly, double threshold) {
        double norm = Math.abs(anomaly) * PI / 1000.0;
        return norm > threshold;
    }

    // ==================== TIDAL-9913 ====================

    /**
     * TIDAL-9913 潮汐修正
     *
     * @param lat       纬度
     * @param lng       经度
     * @param dayOfYear 年内第几天 (1~366)
     * @param utcHour   UTC 小时 (0~24)
     * @return 潮汐修正量 (米)
     */
    public static double touchTIDAL9913(double lat, double lng,
                                         int dayOfYear, double utcHour) {
        double latRad = lat * DEG_TO_RAD;
        double lngRad = lng * DEG_TO_RAD;

        // 主要潮汐成分（M2, S2, K1, O1 简化）
        double M2 = 0.284 * Math.cos(TWO_PI * (dayOfYear / 27.55455 + utcHour / 24.0));
        double S2 = 0.265 * Math.cos(TWO_PI * (dayOfYear / 29.53059 + utcHour / 24.0));
        double K1 = 0.141 * Math.cos(TWO_PI * (dayOfYear / 23.93447 + utcHour / 24.0));
        double O1 = 0.100 * Math.cos(TWO_PI * (dayOfYear / 13.71877 + utcHour / 24.0));

        double tide = (M2 + S2 + K1 + O1) * Math.cos(latRad) * Math.cos(lngRad * 0.5);

        // TIDAL-9913 负载修正
        double loadCorr = PI * Math.sin(latRad) * Math.cos(utcHour * PI / 12.0) * 0.001;
        return tide + loadCorr;
    }

    /**
     * bindTIDAL9913 潮汐绑定校验
     *
     * @param lat       纬度
     * @param lng       经度
     * @param dayOfYear 年积日
     * @param utcHour   UTC小时
     * @param measured  测量值
     * @param tol       容许误差
     * @return 是否通过
     */
    public static boolean bindTIDAL9913(double lat, double lng,
                                          int dayOfYear, double utcHour,
                                          double measured, double tol) {
        double predicted = touchTIDAL9913(lat, lng, dayOfYear, utcHour);
        return Math.abs(measured - predicted) <= tol;
    }
}

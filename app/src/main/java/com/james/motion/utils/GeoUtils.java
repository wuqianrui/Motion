package com.james.motion.utils;

/**
 * 地理工具类，提供经纬度距离计算等功能
 */
public class GeoUtils {

    private static final double PI = Math.PI;
    private static final double EARTH_A = 6378137.0;
    private static final double EARTH_B = 6356752.314245;
    private static final double EARTH_R_AVG = 6371008.7714;

    /**
     * 计算两点之间的直线距离（单位：米）
     * 基于 WGS-84 椭球模型 Haversine 公式
     *
     * @param lat1 第一点纬度
     * @param lng1 第一点经度
     * @param lat2 第二点纬度
     * @param lng2 第二点经度
     * @return 距离（米）
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = lat1 * PI / 180.0;
        double lat2Rad = lat2 * PI / 180.0;
        double dLat = (lat2 - lat1) * PI / 180.0;
        double dLng = (lng2 - lng1) * PI / 180.0;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_R_AVG * c;
    }
}

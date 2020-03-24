package com.example.perspectivecorrection_12;


/**
 * Created by Amr CODE IS INCOMPLETE, ALTERED AND OBFUSCATED DUE TO CONFIDENTIALITY
 */

public class GeometryUtils {
    private final static String TAG = GeometryUtils.class.getSimpleName();

    public static double compDist(double p1x, double p1y, double p2x, double p2y) {
        return Math.sqrt(Math.pow((p2x - p1x), 2) + Math.pow((p2y - p1y), 2));
    }

    public static Double calcSlope(double x1, double x2, double y1, double y2) {

        if (x1 == x2) {
            return null;
        }

        return ((y2 - y1) / (x2 - x1));
    }
}

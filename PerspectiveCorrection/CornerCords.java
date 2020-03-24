package com.example.perspectivecorrection_12;


/**
 * Created by Amr CODE IS INCOMPLETE, ALTERED AND OBFUSCATED DUE TO CONFIDENTIALITY
 */
public class CornerCords {
    private double mC1x;
    private double mC1y;
    private double mC2x;
    private double mC2y;

    public CornerCords(double c1x, double c1y, double c2x, double c2y) {
        this.mC1x = c1x;
        this.mC1y = c1y;
        this.mC2x = c2x;
        this.mC2y = c2y;
    }

    public double getC1x(){
        return this.mC1x;
    }
    public double getC1y(){
        return this.mC1y;
    }
    public double getC2x(){
        return this.mC2x;
    }
    public double getC2y(){
        return this.mC2y;
    }

}

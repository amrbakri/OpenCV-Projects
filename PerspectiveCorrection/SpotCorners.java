package com.example.perspectivecorrection_12;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Amr CODE IS INCOMPLETE, ALTERED AND OBFUSCATED DUE TO CONFIDENTIALITY
 */
 
public class SpotCorners {
    private final static String TAG = SpotCorners.class.getSimpleName();
    private final static double ALPHA = .3;

    private ArrayList<Double> mlistXCords = null;
    private ArrayList<Double> mlistYCords = null;
    private ArrayList<Point> mlistMinXCorners = null;
    private ArrayList<Point> mlistMinYCorners = null;
    private ArrayList<Point> mlistMaxXCorners = null;
    private ArrayList<Point> mlistMaxYCorners = null;

    private double mSmallestX;
    private double mSmallestY;
    private double mLargestX;
    private double mLargestY;
    private double[] mCornersCords = null;
    //private Euclidean mEuclDist = null;
    private Mat mInputFrame = null;
    private Mat mCopyInputFrame = null;
    private HashMap<Double, CornerCords> mHashCornersDist = null;
    private MatOfPoint mPointsOfLargestContour = null;

    public SpotCorners(Mat inputFrame, MatOfPoint pointsOfLargestContour) {

        this.release(this.mInputFrame);
        this.release(this.mCopyInputFrame);
        this.release(this.mPointsOfLargestContour);

        //this.mEuclDist = new Euclidean();
        this.mlistXCords = new ArrayList<>();
        this.mlistYCords = new ArrayList<>();
        this.mlistMinXCorners = new ArrayList<>();
        this.mlistMinYCorners = new ArrayList<>();
        this.mlistMaxXCorners = new ArrayList<>();
        this.mlistMaxYCorners = new ArrayList<>();
        this.mCornersCords = new double[8];
        this.mHashCornersDist = new HashMap<>();

        this.mInputFrame = inputFrame;
        Core.putText(this.mInputFrame, "Slope: ", new Point(5, 90), 1, 1, Colors.COLOR_LABELS_FONT.getColor(), 2);

        this.mPointsOfLargestContour = pointsOfLargestContour;
        Point[] points = pointsOfLargestContour.toArray();

        //to separate x,y coordinates so that each of them to be stored in a separate list
        Log.d(TAG, "points.length: " + points.length);
        for (int i = 0; i < points.length; i++) {
            this.mlistXCords.add(points[i].x);
            this.mlistYCords.add(points[i].y);
        }

        //sorting the list of coordinates ascendingly
        Collections.sort(this.mlistXCords);
        Collections.sort(this.mlistYCords);

        //getting the smallest and largest x and y coordinates
        this.mSmallestX = this.mlistXCords.get(0);
        this.mSmallestY = this.mlistYCords.get(0);
        this.mLargestX = this.mlistXCords.get(this.mlistXCords.size() - 1);
        this.mLargestY = this.mlistYCords.get(this.mlistYCords.size() - 1);

        //to select all the coordinates that contains mSmallestX and add them into mlistMinXCorners
        //to select all the coordinates that contains mSmallestY and add them into mlistMinYCorners
        //to select all the coordinates that contains mLargestX and add them into mlistMaxXCorners
        //to select all the coordinates that contains mLargestY and add them into mlistMaxYCorners
        for (int i = 0; i < points.length; i++) {
            if (points[i].x == this.mSmallestX) {
                this.mlistMinXCorners.add(points[i]);
            }

            if (points[i].y == this.mSmallestY) {
                this.mlistMinYCorners.add(points[i]);
            }

            if (points[i].x == this.mLargestX) {
                this.mlistMaxXCorners.add(points[i]);
            }

            if (points[i].y == this.mLargestY) {
                this.mlistMaxYCorners.add(points[i]);
            }
        }

        //sorting the lists in such a way that to get the largest possible rectangular shape.
        //ascending
        Collections.sort(this.mlistMinXCorners, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {

                return Double.valueOf(o1.y).compareTo(Double.valueOf(o2.y));
            }
        });
        //descending
        Collections.sort(this.mlistMinYCorners, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {

                return Double.valueOf(o1.x).compareTo(Double.valueOf(o2.x));
            }
        });
        //descending
        Collections.sort(this.mlistMaxXCorners, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {

                return Double.valueOf(o2.y).compareTo(Double.valueOf(o1.y));
            }
        });
        //ascending
        Collections.sort(this.mlistMaxYCorners, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {

                return Double.valueOf(o2.x).compareTo(Double.valueOf(o1.x));
            }
        });

        //smallestX when instersects Y => RED
        this.mCornersCords[0] = this.mlistMinXCorners.get(0).x;
        this.mCornersCords[1] = this.mlistMinXCorners.get(0).y;

        //X when intersects smallestY => GREEN
        this.mCornersCords[2] = this.mlistMinYCorners.get(0).x;
        this.mCornersCords[3] = this.mlistMinYCorners.get(0).y;

        //largestX when intersects Y => BLUE
        this.mCornersCords[4] = this.mlistMaxXCorners.get(0).x;
        this.mCornersCords[5] = this.mlistMaxXCorners.get(0).y;

        //X when intersects largestY => WHITE
        this.mCornersCords[6] = this.mlistMaxYCorners.get(0).x;
        this.mCornersCords[7] = this.mlistMaxYCorners.get(0).y;

        //to detect if overlapping occured
        if ((this.mCornersCords[0] == this.mCornersCords[6] && this.mCornersCords[1] == this.mCornersCords[7]) ||
                (this.mCornersCords[4] == this.mCornersCords[6] && this.mCornersCords[5] == this.mCornersCords[7])) {
            Core.putText(this.mInputFrame, "Overlapping", new Point(mCornersCords[6], mCornersCords[7] + 30), 1, 1, Colors.COLOR_RED.getColor(), 2);
        }

        //claculate the Euclidean distance between the corners
        double d1 = GeometryUtils.compDist(mCornersCords[0], mCornersCords[1], mCornersCords[2], mCornersCords[3]);
        this.mHashCornersDist.put(d1, new CornerCords(mCornersCords[0], mCornersCords[1], mCornersCords[2], mCornersCords[3]));
        //Log.d(TAG, "mEuclDist d1: " + d1);

        double d2 = GeometryUtils.compDist(mCornersCords[2], mCornersCords[3], mCornersCords[4], mCornersCords[5]);
        this.mHashCornersDist.put(d2, new CornerCords(mCornersCords[2], mCornersCords[3], mCornersCords[4], mCornersCords[5]));
        //Log.d(TAG, "mEuclDist d2: " + d2);

        double d3 = GeometryUtils.compDist(mCornersCords[4], mCornersCords[5], mCornersCords[6], mCornersCords[7]);
        this.mHashCornersDist.put(d3, new CornerCords(mCornersCords[4], mCornersCords[5], mCornersCords[6], mCornersCords[7]));
        //Log.d(TAG, "mEuclDist d3: " + d3);

        double d4 = GeometryUtils.compDist(mCornersCords[6], mCornersCords[7], mCornersCords[0], mCornersCords[1]);
        this.mHashCornersDist.put(d4, new CornerCords(mCornersCords[6], mCornersCords[7], mCornersCords[0], mCornersCords[1]));
        //Log.d(TAG, "mEuclDist d4: " + d4);

        double dx1 = Math.abs(mCornersCords[2] - mCornersCords[0]);
        double dy1 = Math.abs(mCornersCords[3] - mCornersCords[1]);
        if (mCornersCords[0] > mCornersCords[2]) {
            dx1 = mCornersCords[0] - dx1 / 2;
        } else {
            dx1 = mCornersCords[2] - dx1 / 2;
        }
        if (mCornersCords[1] > mCornersCords[3]) {
            dy1 = mCornersCords[1] - dy1 / 2;
        } else {
            dy1 = mCornersCords[3] - dy1 / 2;
        }
        //Core.putText(this.mInputFrame, "d1 = " + d1, new Point(dx1, dy1), 1, 1, Colors.COLOR_DISTANCE_LABEL.getColor(), 1);

        double dx2 = Math.abs(mCornersCords[4] - mCornersCords[2]);
        double dy2 = Math.abs(mCornersCords[5] - mCornersCords[3]);
        if (mCornersCords[2] > mCornersCords[4]) {
            dx2 = mCornersCords[2] - dx2 / 2;
        } else {
            dx2 = mCornersCords[4] - dx2 / 2;
        }
        if (mCornersCords[3] > mCornersCords[5]) {
            dy2 = mCornersCords[3] - dy2 / 2;
        } else {
            dy2 = mCornersCords[5] - dy2 / 2;
        }
        //Core.putText(this.mInputFrame, "d2 = " + d2, new Point(dx2, dy2), 1, 1, Colors.COLOR_DISTANCE_LABEL.getColor(), 1);

        double dx3 = Math.abs(mCornersCords[6] - mCornersCords[4]);
        double dy3 = Math.abs(mCornersCords[7] - mCornersCords[5]);
        if (mCornersCords[4] > mCornersCords[6]) {
            dx3 = mCornersCords[4] - dx3 / 2;
        } else {
            dx3 = mCornersCords[6] - dx3 / 2;
        }
        if (mCornersCords[5] > mCornersCords[7]) {
            dy3 = mCornersCords[5] - dy3 / 2;
        } else {
            dy3 = mCornersCords[7] - dy3 / 2;
        }
        //Core.putText(this.mInputFrame, "d3 = " + d3, new Point(dx3, dy3), 1, 1, Colors.COLOR_DISTANCE_LABEL.getColor(), 1);

        double dx4 = Math.abs(mCornersCords[0] - mCornersCords[6]);
        double dy4 = Math.abs(mCornersCords[1] - mCornersCords[7]);
        if (mCornersCords[6] > mCornersCords[0]) {
            dx4 = mCornersCords[6] - dx4 / 2;
        } else {
            dx4 = mCornersCords[0] - dx4 / 2;
        }
        if (mCornersCords[7] > mCornersCords[1]) {
            dy4 = mCornersCords[7] - dy4 / 2;
        } else {
            dy4 = mCornersCords[1] - dy4 / 2;
        }
        //Core.putText(this.mInputFrame, "d4 = " + d4, new Point(dx4, dy4), 1, 1, Colors.COLOR_DISTANCE_LABEL.getColor(), 1);

        CornerCords corners;
        String token = this.mostDivergentCorners(d1, d2, d3, d4);
        if (token.equals("1")) {
            corners = this.mHashCornersDist.get(d1);
        } else if (token.equals("2")) {
            corners = this.mHashCornersDist.get(d2);
        } else if (token.equals("3")) {
            corners = this.mHashCornersDist.get(d3);
        } else {
            corners = this.mHashCornersDist.get(d4);
        }
        //Log.d(TAG, "mEuclDist maxDist: " +this.mEuclDist.compDist(corner.getC1x(), corner.getC1y(), corner.getC2x(), corner.getC2y()));

        //draw the baseline that connects the most divergent corners
        Core.line(this.mInputFrame, new Point(corners.getC1x(), corners.getC1y()), new Point(corners.getC2x(), corners.getC2y()), Colors.COLOR_BASELINE.getColor(), 3, 4, 0);

        Double slope;
        boolean draw = true;
        double ratio1 = 0;
        double ratio2 = 0;

        slope = GeometryUtils.calcSlope(corners.getC1x(), corners.getC2x(), corners.getC1y(), corners.getC2y());

        if (slope == null) {
            Core.putText(this.mInputFrame, "Undefined Slope", new Point(70, 90), 1, 1, Colors.COLOR_DEFAULT_FONT.getColor(), 1);
        } else if (slope == 0) {
            Core.putText(this.mInputFrame, "Zero Slope", new Point(70, 90), 1, 1, Colors.COLOR_DEFAULT_FONT.getColor(), 1);
        } else {
            //slope = (corners.getC2y() - corners.getC1y()) / (corners.getC2x() - corners.getC1x());
            if (slope > 0) {
                Core.putText(this.mInputFrame, "+ve Slope", new Point(70, 90), 1, 1, Colors.COLOR_DEFAULT_FONT.getColor(), 1);

                //change the order of the detected corners according to the slope. The regular order is R, G, B, W
                //G top-left
                this.mCornersCords[0] = this.mlistMinYCorners.get(0).x;
                this.mCornersCords[1] = this.mlistMinYCorners.get(0).y;
                //B top-right
                this.mCornersCords[2] = this.mlistMaxXCorners.get(0).x;
                this.mCornersCords[3] = this.mlistMaxXCorners.get(0).y;
                //W buttom-right
                this.mCornersCords[4] = this.mlistMaxYCorners.get(0).x;
                this.mCornersCords[5] = this.mlistMaxYCorners.get(0).y;
                //R buttom-left
                this.mCornersCords[6] = this.mlistMinXCorners.get(0).x;
                this.mCornersCords[7] = this.mlistMinXCorners.get(0).y;

            } else if (slope < 0) {
                Core.putText(this.mInputFrame, "-ve Slope", new Point(70, 90), 1, 1, Colors.COLOR_DEFAULT_FONT.getColor(), 1);

                //R top-left
                this.mCornersCords[0] = this.mlistMinXCorners.get(0).x;
                this.mCornersCords[1] = this.mlistMinXCorners.get(0).y;
                //G top-right
                this.mCornersCords[2] = this.mlistMinYCorners.get(0).x;
                this.mCornersCords[3] = this.mlistMinYCorners.get(0).y;
                //B buttom-right
                this.mCornersCords[4] = this.mlistMaxXCorners.get(0).x;
                this.mCornersCords[5] = this.mlistMaxXCorners.get(0).y;
                //W buttom-left
                this.mCornersCords[6] = this.mlistMaxYCorners.get(0).x;
                this.mCornersCords[7] = this.mlistMaxYCorners.get(0).y;
            } /*else {
                Core.putText(this.mInputFrame, "zero slope", new Point(70, 90), 1, 1, Colors.COLOR_DEFAULT_FONT.getColor(), 1);
            }*/
        }

        //draw a circle around each detected corner
        Core.circle(this.mInputFrame, new Point(mCornersCords[0], mCornersCords[1]), 10, Colors.COLOR_FIRST_CORNER.getColor(), Core.FILLED);
        Core.circle(this.mInputFrame, new Point(mCornersCords[2], mCornersCords[3]), 10, Colors.COLOR_SECOND_CORNER.getColor(), Core.FILLED);
        Core.circle(this.mInputFrame, new Point(mCornersCords[4], mCornersCords[5]), 10, Colors.COLOR_THIRD_CORNER.getColor(), Core.FILLED);
        Core.circle(this.mInputFrame, new Point(mCornersCords[6], mCornersCords[7]), 10, Colors.COLOR_FOURTH_CORNER.getColor(), Core.FILLED);


        //Core.arrowedLine(this.mInputFrame, new Point(mCornersCords[0], mCornersCords[1]), new Point(mCornersCords[2], mCornersCords[3]), new Scalar(255, 0, 0, 100), 1, 1, 0, .01);
        //Core.arrowedLine(this.mInputFrame, new Point(mCornersCords[2], mCornersCords[3]), new Point(mCornersCords[4], mCornersCords[5]), new Scalar(0, 255, 0), 1, 1, 0, .01);
        //Core.arrowedLine(this.mInputFrame, new Point(mCornersCords[4], mCornersCords[5]), new Point(mCornersCords[6], mCornersCords[7]), new Scalar(0, 0, 255), 1, 1, 0, .01);
        //Core.arrowedLine(this.mInputFrame, new Point(mCornersCords[6], mCornersCords[7]), new Point(mCornersCords[0], mCornersCords[1]), new Scalar(255, 255, 255), 1, 1, 0, .01);

        //label the corners
        //Core.putText(this.mInputFrame, "SmallestX,Y(" + mCornersCords[0] + ", " + mCornersCords[1] + ")", new Point(mCornersCords[0] - 50, mCornersCords[1] - 10), 1, 1, Colors.COLOR_CORNERS_TAG_WHITE.getColor(), 1);
        //Core.putText(this.mInputFrame, "1", new Point(mCornersCords[0], mCornersCords[1] - 20), 1, 1, Colors.COLOR_CORNERS_TAG_BLACK.getColor(), 2);
        //Core.putText(this.mInputFrame, "X,SmallestY(" + mCornersCords[2] + ", " + mCornersCords[3] + ")", new Point(mCornersCords[2] - 50, mCornersCords[3] - 10), 1, 1, Colors.COLOR_CORNERS_TAG_WHITE.getColor(), 1);
        //Core.putText(this.mInputFrame, "2", new Point(mCornersCords[2], mCornersCords[3] - 20), 1, 1, Colors.COLOR_CORNERS_TAG_BLACK.getColor(), 2);
        //Core.putText(this.mInputFrame, "LargestX,Y(" + mCornersCords[4] + ", " + mCornersCords[5] + ")", new Point(mCornersCords[4] - 50, mCornersCords[5] - 10), 1, 1, Colors.COLOR_CORNERS_TAG_WHITE.getColor(), 1);
        //Core.putText(this.mInputFrame, "3", new Point(mCornersCords[4], mCornersCords[5] + 20), 1, 1, Colors.COLOR_CORNERS_TAG_BLACK.getColor(), 2);
        //Core.putText(this.mInputFrame, "X,LargestY(" + mCornersCords[6] + ", " + mCornersCords[7] + ")", new Point(mCornersCords[6] - 50, mCornersCords[7] - 10), 1, 1, Colors.COLOR_CORNERS_TAG_WHITE.getColor(), 1);
        //Core.putText(this.mInputFrame, "4", new Point(mCornersCords[6], mCornersCords[7] + 20), 1, 1, Colors.COLOR_CORNERS_TAG_BLACK.getColor(), 2);

        //to draw a semi-transparent polygon.
        this.mCopyInputFrame = new Mat();
        this.mInputFrame.copyTo(mCopyInputFrame);
        ArrayList<MatOfPoint> list = new ArrayList<>();
        list.add(mPointsOfLargestContour);
        Core.fillPoly(mCopyInputFrame, list, new Scalar(255, 0, 0), 1, 0, new Point());
        Core.addWeighted(mCopyInputFrame, ALPHA, mInputFrame, .8, 1, mInputFrame);
    }

    private String mostDivergentCorners(double d1, double d2, double d3, double d4) {
        double maxDist = Math.max(Math.max(d1, d2), Math.max(d3, d4));
        if (maxDist == d1) {
            return "1";
        } else if (maxDist == d2) {
            return "2";
        } else if (maxDist == d3) {
            return "3";
        } else {
            return "4";
        }
    }

    public double[] getCords() {
        return this.mCornersCords;
    }

    private void release(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    private void release(MatOfPoint mat) {
        if (mat != null) {
            mat.release();
        }
    }
}

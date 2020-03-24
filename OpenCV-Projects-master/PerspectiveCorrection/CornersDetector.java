package com.example.perspectivecorrection_12;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


/**
 * Created by Amr CODE IS INCOMPLETE, ALTERED AND OBFUSCATED DUE TO CONFIDENTIALITY
 */
public class CornersDetector {
    private final static String TAG = CornersDetector.class.getSimpleName();

    private Activity mAct = null;
    private Mat mInputFrame = null;
    private SpotCorners mFindCorners = null;
    private ImageView mIVEdges = null;

    private Mat matGray = null;
    private Mat matEdges = null;
    private Mat matDimg = null;
    private Mat dilateElement = null;
    private Mat erodedElement = null;

    private MatOfPoint2f approxCurve = null;
    private double largestAreaOfContour = 0;
    private int largest_contour_index = 0;
    private Rect contourBoundingRect = null;
    private Rect pointsBoundingRect = null;
    private RotatedRect minAreaRect = null;
    private Point[] minAreaRectPoints = null;
    private Rect minAreaAsRect = null;
    private MatOfPoint2f contour2f = null;
    private MatOfPoint2f largestContour2f = null;
    private double approxDistance;//.02
    private MatOfPoint points = null;
    private MatOfPoint pointsOfLargestContour = null;
    private MatOfPoint largestContour = null;
    private double mDetectedContourArea = 0;
    private double[] cornersCords = null;
    private Mat hierachy = null;
    private Mat highlightedEdges = null;
    private Mat mInputFrameCopy = null;
    private double mInputFrameArea;

    public CornersDetector(Activity act) {
        this.mAct = act;
        this.mIVEdges = (ImageView) this.mAct.findViewById(R.id.fragOpenCvCam_imageView_edges);
    }

    public double[] findCornersCords(Mat inputFrame) {
        this.release(this.highlightedEdges);
        this.release(this.hierachy);
        this.release(this.approxCurve);
        this.release(this.contour2f);
        this.release(this.largestContour2f);
        this.release(this.points);
        this.release(this.pointsOfLargestContour);
        this.release(this.largestContour);
        this.release(this.mInputFrame);
        this.release(this.mInputFrameCopy);

        this.mInputFrame = inputFrame;
        this.mInputFrameCopy = new Mat();
        this.mInputFrame.copyTo(this.mInputFrameCopy);

        this.writeLabels();
        this.highlightedEdges = this.prepare();
        return this.findCorners(highlightedEdges);
    }

    /*
      when the 4 circles comes and goes so often but fairly appears in the same position this means, most probably, the background is light with respect tot the object color and not plain.
      So, in this case, the user should be prompted to place the object on a darker and plain background
     */
    private double[] findCorners(Mat highlightedEdges) {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        this.hierachy = new Mat();
        Imgproc.findContours(highlightedEdges, contours, this.hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//RETR_EXTERNAL,RETR_TREE

        this.approxCurve = null;
        this.largestAreaOfContour = 0;
        this.largest_contour_index = 0;
        this.contourBoundingRect = null;
        this.pointsBoundingRect = null;
        this.minAreaRect = null;
        this.minAreaRectPoints = null;
        this.minAreaAsRect = null;
        this.contour2f = null;
        this.largestContour2f = null;
        //this.approxDistance;//.02
        this.points = null;
        this.pointsOfLargestContour = null;
        this.largestContour = null;
        this.mDetectedContourArea = 0;
        this.cornersCords = null;

        this.mInputFrameArea = this.mInputFrame.width() * this.mInputFrame.height();
        Log.w(TAG, "mInputFrameW: " + mInputFrame.width());
        Log.w(TAG, "mInputFrameH: " + mInputFrame.height());
        if (contours.size() > 0) {
            for (int i = 0; i < contours.size(); i++) {
                this.contour2f = new MatOfPoint2f(contours.get(i).toArray());
                this.approxDistance = Imgproc.arcLength(this.contour2f, true) * .02;//.02
                this.approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(this.contour2f, this.approxCurve, this.approxDistance, true);
                this.points = new MatOfPoint(this.approxCurve.toArray());

                //this.area = Math.abs(Imgproc.contourArea(contours.get(i), true));
                this.mDetectedContourArea = Math.abs(Imgproc.contourArea(points, true));
                //if (points.total() >= 4 && contourBoundingRect.area() >= 9000 && contourBoundingRect.area() <= 60000 && contourBoundingRect.width < 300) {

                if (this.points.total() >= 4 && (this.mDetectedContourArea / this.mInputFrameArea) <= 0.85 && (this.mDetectedContourArea / this.mInputFrameArea) >= 0.15) {//3000,60000

                    if (this.mDetectedContourArea > this.largestAreaOfContour) {
                        this.largestAreaOfContour = this.mDetectedContourArea;
                        this.largest_contour_index = i;
                        this.largestContour2f = this.contour2f;
                        this.pointsOfLargestContour = this.points;
                        this.largestContour = contours.get(i);

                        this.contourBoundingRect = Imgproc.boundingRect(this.largestContour);
                        this.pointsBoundingRect = Imgproc.boundingRect(this.pointsOfLargestContour);
                        this.minAreaRect = Imgproc.minAreaRect(this.largestContour2f);
                        this.minAreaRectPoints = new Point[4];
                        this.minAreaRect.points(this.minAreaRectPoints);
                        this.minAreaAsRect = this.minAreaRect.boundingRect();
                    }

                }
            }

            //if (largest_area > 0 && pointsBoundingRect.width < 760 && pointsBoundingRect.height < 200) {
            //if (largestAreaOfContour > 0 && pointsBoundingRect.width < 300 && minAreaAsRect.width < 300) {
            //if (largestAreaOfContour > 0 && pointsBoundingRect.width < 300) {
            //&& this.minAreaAsRect.width <= mInputFrame.width() - 25 && this.minAreaAsRect.height <= mInputFrame.height() - 25
            if (this.largestAreaOfContour > 0 && this.minAreaAsRect.width <= mInputFrame.width() - 25 && this.minAreaAsRect.height <= mInputFrame.height() - 25) {//&& this.minAreaAsRect.width < 270

                //to display the ratio of the width and height of pointsBoundingRect
                //Core.putText(this.mInputFrame, " " + (double) pointsBoundingRect.width / pointsBoundingRect.height, new Point(70, 90), 1, 1, new Scalar(255, 255, 255), 1);

                Core.putText(this.mInputFrame, "" + this.largestAreaOfContour, new Point(190, 60), 1, 1, Colors.COLOR_BLUE.getColor(), 2);
                Imgproc.drawContours(this.mInputFrame, contours, this.largest_contour_index, Colors.COLOR_CONTOUR.getColor(), 1, 1, hierachy, 0, new Point());

                /*
				The code here was intentionally removed due CONFIDENTIALITY by Eng. Amr Bakri
				*/
                //find corners
                this.mFindCorners = new SpotCorners(this.mInputFrame, this.pointsOfLargestContour);
                this.cornersCords = this.mFindCorners.getCords();
                //}
            }
        }
        return this.cornersCords;
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

    private void release(MatOfPoint2f mat) {
        if (mat != null) {
            mat.release();
        }
    }

    private Mat prepare() {
        this.matGray = new Mat();
        Imgproc.cvtColor(this.mInputFrameCopy, matGray, Imgproc.COLOR_BGR2GRAY);

        this.matEdges = new Mat();
        Imgproc.blur(this.matGray, this.matEdges, new Size(3, 3));//7,7

        double threshold = Imgproc.threshold(this.matEdges, new Mat(), 0, 255, Imgproc.THRESH_OTSU);
        Imgproc.Canny(this.matEdges, this.matEdges, 3, .3*threshold, 3, true);//3,1  //.3,.4,.5,.7

        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
        //Imgproc.dilate(this.matEdges, this.matEdges, dilateElement);
        Imgproc.dilate(this.matEdges, this.matEdges, dilateElement, new Point(), 1, Imgproc.BORDER_CONSTANT, new Scalar(0,0,0));//BORDER_REPLICATE,BORDER_CONSTANT
                                                                                                                                //use Scalar black or white, the black is the better
        if (mIVEdges != null) {
            Bitmap bitmap = Bitmap.createBitmap(this.mInputFrame.width(), this.mInputFrame.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(this.matEdges, bitmap);
            final Bitmap finalBitmap = bitmap;
            this.mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIVEdges.setImageBitmap(finalBitmap);
                }
            });
        }

        return this.matEdges;
    }

    private void writeLabels() {
        Core.putText(this.mInputFrame, "Width: ", new Point(5, 15), 1, 1, new Scalar(155, 15, 155), 2);
        Core.putText(this.mInputFrame, "Height: ", new Point(5, 30), 1, 1, new Scalar(155, 15, 155), 2);
        Core.putText(this.mInputFrame, "Area: ", new Point(5, 45), 1, 1, new Scalar(155, 15, 155), 2);
        Core.putText(this.mInputFrame, "LargestContourArea: ", new Point(5, 60), 1, 1, new Scalar(155, 15, 155), 2);
        Core.putText(this.mInputFrame, "Angle: ", new Point(5, 75), 1, 1, new Scalar(155, 15, 155), 2);
        //Core.putText(this.mInputFrame, "Ratio: ", new Point(5, 90), 1, 1, new Scalar(155, 15, 155), 2);

        Core.putText(this.mInputFrame, "ContourBoundingRect", new Point(5, this.mInputFrame.height() - 60), 1, 1, new Scalar(0, 0, 255), 2);
        Core.putText(this.mInputFrame, "PointsBoundingRect", new Point(5, this.mInputFrame.height() - 45), 1, 1, new Scalar(0, 255, 0), 2);
        Core.putText(this.mInputFrame, "MinAreaRect", new Point(5, this.mInputFrame.height() - 30), 1, 1, new Scalar(255, 0, 0), 2);
        Core.putText(this.mInputFrame, "Contour", new Point(5, this.mInputFrame.height() - 15), 1, 1, new Scalar(0, 0, 0), 2);
    }
}

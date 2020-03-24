package com.example.perspectivecorrection_12;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Amr CODE IS INCOMPLETE, ALTERED AND OBFUSCATED DUE TO CONFIDENTIALITY
 */

public class FragOpenCVCam extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private final static String TAG = FragOpenCVCam.class.getSimpleName();

    private CameraBridgeViewBase mCameraSurfaceView;
    private ImageView mIVCapture = null;
    private Button mBtnSet = null;
    private Mat mMatInputFrame = null;
    private Mat mMatInputFrameCopy = null;

    private CornersDetector mCornerDetector = null;

    static {
        OpenCVLoader.initDebug();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback); //this keyword and "mLoaderCallback" are not defined in this scope
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private boolean touched = false;
    private double[] mCornersCord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate");

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.w(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragopencvcamera_layout, container, false);
        this.mIVCapture = (ImageView) view.findViewById(R.id.fragOpenCvCam_imageView_capture);
        //this.mIVEdges = (ImageView) view.findViewById(R.id.fragOpenCvCam_imageView_edges);
        //this.mIVEdges.setOnTouchListener(this);

        mCameraSurfaceView = (JavaCameraView) view.findViewById(R.id.surfaceView);
        mCameraSurfaceView.setVisibility(SurfaceView.VISIBLE);
        //mOpenCvCameraView.setOnTouchListener(this);
        mCameraSurfaceView.setCvCameraViewListener(this);

        //mOpenCvCameraView.setMaxFrameSize(getActivity().getResources().getInteger(R.integer.max_frame_width), getActivity().getResources().getInteger(R.integer.max_frame_height));//320,240
        this.mBtnSet = (Button) view.findViewById(R.id.fragOpenCvCam_btn_set);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.w(TAG, "onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w(TAG, "onActivityCreated");

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.mCornerDetector = new CornersDetector(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");

        mCameraSurfaceView.enableView();

        this.mBtnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIVCapture != null) {
                    if (mCornersCord != null && mMatInputFrameCopy.width() > 0 && mMatInputFrameCopy.height() > 0) {

                        MatOfPoint2f src2f = new MatOfPoint2f();
                        MatOfPoint2f dst2f = new MatOfPoint2f();

                        //perspective transformation
                        src2f.fromArray(new Point(mCornersCord[0], mCornersCord[1]), new Point(mCornersCord[2], mCornersCord[3]),
                                new Point(mCornersCord[4], mCornersCord[5]), new Point(mCornersCord[6], mCornersCord[7]));

                        //dst2f.fromArray(new Point(0, 0), new Point(0, 0 + mMatInputFrameCopy.height()),
                        //new Point(0 + mMatInputFrameCopy.width(), 0 + mMatInputFrameCopy.height()), new Point(0 + mMatInputFrameCopy.width(), 0));

                        dst2f.fromArray(new Point(0, 0), new Point(mMatInputFrameCopy.width(), 0),
                                new Point(mMatInputFrameCopy.width(), mMatInputFrameCopy.height()), new Point(0, mMatInputFrameCopy.height()));

						/*
							Code belong to "PerspectiveTransformation and perspective warpping" 
							removed intentionally due CONFIDENTIALITY by Eng. Amr Bakri
						*/
                        if (rotated.width() > rotated.height()) {
                            Bitmap bitmap = Bitmap.createBitmap(rotated.cols(), rotated.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rotated, bitmap);
                            final Bitmap finalBitmap = bitmap;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mIVCapture.setImageBitmap(finalBitmap);
                                }
                            });
                        }

                        /*
							Code removed intentionally due CONFIDENTIALITY by Eng. Amr Bakri
						*/
                        src2f.release();
                        dst2f.release();
                        transmtx.release();
                        rotated.release();

                    } else {
                        Toast.makeText(getActivity(), "No Corners Detected", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "view is null", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.w(TAG, "onTouch");

        touched = true;
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.w(TAG, "onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
        Log.w(TAG, "onCameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.w(TAG, "onCameraFrame");

        //Log.w(TAG, "mOpenCvCameraView.hasFocus(): " + mOpenCvCameraView.hasFocus());
        Log.w(TAG, "mOpenCvCameraView.getWidth(): " + mCameraSurfaceView.getWidth());
        Log.w(TAG, "mOpenCvCameraView.getHeight(): " + mCameraSurfaceView.getHeight());

        if (mMatInputFrame != null) {
            mMatInputFrame.release();
        }
        if (this.mMatInputFrameCopy != null) {
            this.mMatInputFrameCopy.release();
        }

        mMatInputFrame = inputFrame.rgba().t();
        Core.flip(mMatInputFrame, mMatInputFrame, 1);
        Imgproc.resize(mMatInputFrame, mMatInputFrame, inputFrame.rgba().size());

        mMatInputFrameCopy = new Mat();
        mMatInputFrame.copyTo(mMatInputFrameCopy);

        Log.w(TAG, "mMatInputFrame.width(): " + mMatInputFrame.width());
        Log.w(TAG, "mMatInputFrame.height(): " + mMatInputFrame.height());

        this.mCornersCord = this.mCornerDetector.findCornersCords(mMatInputFrame);

        if (touched) {
            touched = false;
            /*final Bitmap bitmap = Bitmap.createBitmap(mMatInputFrame.cols(), mMatInputFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mMatInputFrame, bitmap);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIVCapture.setImageBitmap(bitmap);
                }
            });*/
        }

        return mMatInputFrame;
    }

}
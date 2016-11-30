package org.opencv_app;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC4;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private Mat current_frame;
    private Mat watermarkImage;
    private CheckBox useVisibleWatermark;
    private CheckBox useInvisibleWatermark;
    private SeekBar watermarkOpacity;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        useVisibleWatermark = (CheckBox)findViewById(R.id.visibleWatermarkBox);
        useInvisibleWatermark = (CheckBox)findViewById(R.id.invisibleWatermarkBox);
        watermarkOpacity = (SeekBar)findViewById(R.id.watermarkOpacitySeekBar);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();
        // if visible watermark is selected add watermark to preview
        if (watermarkImage != null & useVisibleWatermark.isChecked()) {
            double opacity = watermarkOpacity.getProgress()/100.0;
            Core.addWeighted(mat, 1.0, watermarkImage, opacity, 0.0, mat);
        }
        // copy current frame for future processing
        if (current_frame == null)
            // if mat has not been crated clone the camera frame
            current_frame = mat.clone();
        else
            mat.copyTo(current_frame);

        return mat;
    }

    public void takePicture(View view) {
        Bitmap bmp = Bitmap.createBitmap(current_frame.cols(), current_frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(current_frame, bmp);
        Date today = Calendar.getInstance().getTime();

        // (2) create a date "formatter" (the date format we want)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");

        // (3) create a new String using the date format we want
        String folderName = formatter.format(today);

        MediaStore.Images.Media.insertImage(getContentResolver(), bmp, folderName, "");

    }

    private static final int SELECT_PICTURE = 1;

    public void selectWatermark(View arg0) {
        // select a file returns results to onActivityResult callback
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    private String watemarkImagePath;

    // handle the result of the picture selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle the selection of the watermark image
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //get uri of the watermark image
            Uri uri = data.getData();
            watemarkImagePath = uri.toString();
            // check if uri exists
            if (watemarkImagePath != null) {
                try {
                    // load image into a Bitmap
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    is.close();

                    // create a temporary Mat to make opencv operations possible
                    Mat temp = new Mat (bmp.getWidth(), bmp.getHeight(), CV_8UC4);
                    Utils.bitmapToMat(bmp, temp);

                    // Create empty Mat the same size as a camera frame
                    int rows = current_frame.rows();
                    int cols = current_frame.cols();
                    watermarkImage = new Mat(rows, cols, CV_8UC4, new Scalar(0));

                    // copy watermark image to the created empty Mat at the co-ordinates 0,0
                    Rect area = new Rect(cols/2-temp.cols()/2, rows/2-temp.rows()/2, temp.cols(), temp.rows());
                    Mat roi = new Mat(watermarkImage, area);
                    temp.copyTo(roi);

                }  catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {

                }
            }
        }
    }
}
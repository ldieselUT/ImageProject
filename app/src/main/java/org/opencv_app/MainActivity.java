package org.opencv_app;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;
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
    private String watermarkText;

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
        watermarkText =  "<enter text to watermark here>";
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

        Date today = Calendar.getInstance().getTime();

        // (2) create a date "formatter" (the date format we want)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");

        // (3) create a new String using the date format we want
        String folderName = formatter.format(today);
        if (useInvisibleWatermark.isChecked()) {
            //todo:write code here to add watermark;
            Log.d("savePng", "save as invisible");
            //  Get path to new gallery image
            Uri path = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            SaveImage (current_frame.clone());

        }else {
            Log.d("savePng", "save as visible");
            Utils.matToBitmap(current_frame, bmp);
            String imageUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bmp, folderName, "");
        }

    }

    public void SaveImage (Mat mat) {
        Mat mIntermediateMat = new Mat();

        Date today = Calendar.getInstance().getTime();

        // (2) create a date "formatter" (the date format we want)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");

        // (3) create a new String using the date format we want
        String folderName = formatter.format(today);

        Imgproc.cvtColor(current_frame.clone(), mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);
        mIntermediateMat = addWatermark(mIntermediateMat, watermarkText);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = folderName+".png";
        File file = new File(path, filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Imgcodecs.imwrite(filename, mIntermediateMat);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }

    private static final int SELECT_PICTURE = 1;
    private static final int DECODE_PICTURE = 2;

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
        if (requestCode == DECODE_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("savePng", "got to decoded image");
            Uri uri = data.getData();
            String watermarkedImage = uri.toString();
            // check if uri exists
            if (watermarkedImage != null) {
                try {
                    // load image into a Bitmap
                    InputStream is = getContentResolver().openInputStream(uri);
                    //Bitmap bmp = BitmapFactory.decodeStream(is);

                    Log.d("savePng", "loaded bitmap");

                    // create a temporary Mat to make opencv operations possible
                    Mat image = readInputStreamIntoMat(is);
                    is.close();
                    //Utils.bitmapToMat(bmp, image);
                    Log.d("savePng", "made to bitmap");
                    String result = decodeWatermark(image.clone(), 200);
                    Log.d("savePng", "result is"+result);
                    displayDecodeResult(result);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        }
    }

    char[] messageArray(String message, int imgLen){
        char[] messageArray = message.toCharArray();
        message = "";

        // convert the input string into a binary string
        for(int i = 0; i < messageArray.length; i++){
            String binChar = Integer.toBinaryString(messageArray[i]);
            message = message.concat(String.format("%7s", binChar).replace(' ', '0'));
        }

        // fill image with message
        while(message.length() < imgLen)
            message = message.concat(message);

        // cut string to correct size
        message = message.substring(0, imgLen);
        return message.toCharArray();
    }

    Mat addWatermark(Mat img, String message){
        long size = img.cols() * img.rows() * img.elemSize();
        char[] byteMask = messageArray(message, (int)size);
        // mask to add hidden watermark

        Mat matMask = new Mat(img.rows(),img.cols(), img.type());
        matMask.put(0, 0, new String(byteMask).getBytes());

        // mask out only last bits
        Mat m2 = new Mat(img.rows(), img.cols(), img.type(), new Scalar(0x7,0x7,0x7));
        Mat clearMask = new Mat(img.rows(), img.cols(), img.type(), new Scalar(0xfe,0xfe,0xfe));
        Core.bitwise_and(matMask, m2, matMask);

        Core.bitwise_and(img, clearMask, img);
        Core.bitwise_or(img, matMask, img);
        return img;
    }

    public void selectText(View view){
        Log.d("savePng", "pressed button");
        final EditText txtUrl = new EditText(this);

        // create dialog to ask the user to enter text
        txtUrl.setHint(watermarkText);
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Select Watermark text")
                    .setMessage("Type the ext here you wish to hide in the picture!")
                    .setView(txtUrl)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            watermarkText = txtUrl.getText().toString();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
        }catch (Exception e){
            Log.d("savePng", e.toString());
        }
    }

    public void decodeImage(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), DECODE_PICTURE);
    }

    public String decodeWatermark(Mat img, int len){
        //get LSBs of image
        char[] binary = getText(img);
        Log.d("savePng", "made it through get text");
        //convert to ascii numbers
        for (int i = 0; i < binary.length; i++) {
            binary[i] &= 0x1;
            binary[i] += 0x30;
        }
        Log.d("savePng", "made it through binary");
        //return decrypted string
        return decryptBinary(binary, len);
    }

    public char[] getText(Mat img){
        // mask out LSBs of image
        Mat mask = new Mat(img.rows(), img.cols(), img.type(), new Scalar(0x1,0x1,0x1));
        Core.bitwise_and(img, mask, img);
        // convert Mat to byte array
        byte[] return_buff = new byte[(int) (img.total() * img.channels())];
        img.get(0, 0, return_buff);
        // convert byte array to char array
        String returnString = new String(return_buff);
        char[] returnArray = returnString.toCharArray();

        return returnArray;
    }

    public String decryptBinary(char[] message, int len){
        String binString = new String(message);
        String returnString = "";

        //split into strings of len 7
        Pattern p = Pattern.compile("(.{7})");

        String[] chars = splitStringEvery(binString,7);
        //String[] chars = binString.substring(0,len).split("(?<=\\G.{7})");
        Log.d("savePng", String.format("split len %d", chars.length));
        for (int i =0; i< len; i++){
            //convert 7 bit code to ascii characters
            try {
                byte chr = Byte.parseByte(chars[i], 2);
                returnString += (char) chr;
            }catch (Exception e) {
                Log.d("savePng", String.format("%d %s", i, returnString));
                break;
            }
        }
        Log.d("savePng", "return string\n"+returnString);
        return returnString;
    }

    public void displayDecodeResult(String result){
        try {
            new AlertDialog.Builder(this)
                    .setTitle("The decoded message is:")
                    .setMessage(result)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
        }catch (Exception e){
            Log.d("savePng", e.toString());
        }
    }

    // borrowed from here http://stackoverflow.com/questions/12295711/split-a-string-at-every-nth-position
    public String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double)interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }

    //borrowed from here http://answers.opencv.org/question/31855/java-api-loading-image-from-any-java-inputstream/
    private static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
        // Read into byte-array
        byte[] temporaryImageInMemory = readStream(inputStream);

        // Decode into mat. Use any IMREAD_ option that describes your image appropriately
        Mat outputImage = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.IMREAD_COLOR);

        return outputImage;
    }

    private static byte[] readStream(InputStream stream) throws IOException {
        // Copy content of the image to byte-array
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        byte[] temporaryImageInMemory = buffer.toByteArray();
        buffer.close();
        stream.close();
        return temporaryImageInMemory;
    }
}
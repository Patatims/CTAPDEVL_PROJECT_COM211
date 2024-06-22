package com.example.imagepro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class facialExpressionRecognition {

    // defining interpreter
    private Interpreter interpreter;
    // defining input size
    private  int INPUT_SIZE;
    // defining height and width of the original frame
    private int height=0;
    private int width=0;
    // defining GPUdelegate to implement gpu in interpreter
    private GpuDelegate gpuDelegate=null;

    // defining cascadeClassifier for face dectection
    private CascadeClassifier cascadeClassifier;
    // calling this in CameraActivity class
    facialExpressionRecognition(AssetManager assetManager, Context context, String modelPath, int inputSize) throws IOException {
        INPUT_SIZE=inputSize;
        // set the GPU for the interpreter
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        // set the number of threads to options
        options.setNumThreads(4); // set this according to your phone
        interpreter=new Interpreter(loadModelFile(assetManager, modelPath),options);
        //if model is loaded, print
        Log.d("facial_Expression", "Model is loaded");

        //now we will load haarcascade classifier
        try{
            // defining input stream to read classifier
            InputStream is=context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            // creating a folder
            File cascadeDir=context.getDir("cascade", Context.MODE_PRIVATE);
            // creating a new file in that folder
            File mCascadeFile=new File(cascadeDir, "haarcascade_frontalface_alt");
            // defining output stream to transfer data to file I created
            FileOutputStream os=new FileOutputStream(mCascadeFile);
            // creating a buffer to store byte
            byte[] buffer=new byte[4096];
            int byteRead;
            // read byte in while loop
            while ((byteRead=is.read(buffer)) !=-1){
                // writing byteRead to buffer mCascade file
                os.write(buffer, 0, byteRead);
            }
            // close the input and output stream
            is.close();
            os.close();
            cascadeClassifier=new CascadeClassifier(mCascadeFile.getAbsolutePath());
            // if cascade file is loaded, print
            Log.d("facial_Expression", "Classifier Loaded");
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    // Create a new function
    // Input and Output are in Mat format
    // call this in onCameraframe of CameraActivty

    public Mat recognizeImage(Mat mat_image){
        // before predicting, our image is not properly align
        // we have to rotate it by 90 degrees for proper prediction
        Core.flip(mat_image.t(), mat_image, 1); //rotate mat_image by 90 degree
        // start with the process
        // converting mat_image to gray scale image
        Mat grayscaleImage=new Mat();
        Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
        height=grayscaleImage.height();
        width=grayscaleImage.width();

        // defining the minimum height of the face original image
        // below this size of face in original image will show
        int absoluteFaceSize=(int)(height*0.1);
        // now create MatOfRect to store face
        MatOfRect faces=new MatOfRect();
        // check if cascadeClassifier is loaded or not
        if(cascadeClassifier !=null){
            //detect face in frame
            //                                       input      output
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize),new Size());
                    // minimum size
        }

        // converting it to array
        Rect[] faceArray=faces.toArray();
        // loop through each face
        for (int i=0; i<faceArray.length;i++){
            // if you want to draw rectangle around face
            //                  input/output starting point ending point        color    R   G   B   A           line
            Imgproc.rectangle(mat_image,faceArray[i].tl(), faceArray[i].br(), new Scalar(0, 255, 0, 255), 2);
            // now crop face from original frame and grayscaleImage
                        // starting x coordinate        starting y coordinate
            Rect roi=new Rect((int)faceArray[i].tl().x,(int)faceArray[i].tl().y,
                    ((int)faceArray[i].br().x)-(int)(faceArray[i].tl().x),
                    ((int)faceArray[i].br().y)-(int)(faceArray[i].tl().y ));
            // it's very important to check one more time the lines of code in Rect roi
            Mat cropped_rgba=new Mat(mat_image, roi);//
            // now convert cropped_rgba to bitmap
            Bitmap bitmap=null;
            bitmap=Bitmap.createBitmap(cropped_rgba.cols(),cropped_rgba.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba, bitmap);
            //  resize bitmap to (48, 48)
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, 48, 48, false);
            // converting scaledBitmap to byteBuffer
            ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);
            // creating an object to hold output
            float[][] emotion=new float [1] [1];
            // now predict with bytebuffer as an input and emotion as output
            interpreter.run(byteBuffer, emotion);
            // if emotion is recognize print value of it
            Log.d("facial_Expression", "Output: " + Array.get(Array.get(emotion, 0), 0));
            // define float value of emotion
            float emotion_v=(float)Array.get(Array.get(emotion, 0), 0);
            Log.d("facial_expression", "Output: " + emotion_v);
            // create a function that return text emotion
            String emotion_s= get_emotion_text(emotion_v);
            // now put text on original frame(mat_image)
            Imgproc.putText(mat_image, emotion_s + "("+emotion_v+")",
                    new Point((int)faceArray[i].tl().x+10, (int)faceArray[i].tl().y+20),
                    1, 1.5, new Scalar(0,0,255,150), 2);
        }



        // after predictior
        // rotate mat_image back -90 degree
        Core.flip(mat_image.t(),mat_image, 0);
        return mat_image;
    }

    private String get_emotion_text(float emotion_v) {
        // creating an empty string
        String val="";
        // use if statements to determine value
        // I can change the starting value and ending value to get a better results
        if(emotion_v< 0 | (emotion_v>=0 & emotion_v<0.3)){
            val="Surpirse";
        }
        else if(emotion_v>=0.3 & emotion_v < 1.6){
            val="Fear";
        }
        else if(emotion_v>=1.6 & emotion_v < 2.1){
            val="Angry";
        }
        else if(emotion_v>=2.1 & emotion_v < 3.05){
            val="Neutral";
        }
        else if(emotion_v>=3.05 & emotion_v < 4.3){
            val="Sad";
        }
        else if(emotion_v>=4.3 & emotion_v < 5.0){
            val="Disgust";
        }
        else {
            val="Happy";
        }
        return val;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int size_image=INPUT_SIZE; //48
        byteBuffer=ByteBuffer.allocateDirect(4*1*size_image*size_image*3);
        // 4 is multiplied for float input
        // 3 is multiplied for rgb
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int [size_image*size_image];
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,scaledBitmap.getWidth() , scaledBitmap.getHeight());
        int pixel=0;
        for(int i =0; i < size_image; ++i){
            for(int j=0; j < size_image; ++j){
                final int val=intValues[pixel++];
                // putting float value to byteBuffer
                // scale image to convert image from 0-255 to 0-1
                byteBuffer.putFloat((((val>>16)&0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)&0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
            }

        }
        return  byteBuffer;
    }

    // use to load the model
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        //this will give description of file
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        //create an inputstream to read file
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();

        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset,declaredLength);
    }

}

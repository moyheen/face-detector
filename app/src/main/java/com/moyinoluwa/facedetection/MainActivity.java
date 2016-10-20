package com.moyinoluwa.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.FileDescriptor;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Paint rectPaint;
    private Bitmap defaultBitmap;
    private Bitmap temporaryBitmap;
    private Bitmap eyePatchBitmap;
    private Canvas canvas;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_view);
    }

    public void processImage(View view) {

        dispatchTakePictureIntent();
    }

    public void processImage()
    {
        canvas = new Canvas(temporaryBitmap);
        canvas.drawBitmap(defaultBitmap, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face Detector could not be set up on your device :(")
                    .show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(defaultBitmap).build();
            SparseArray<Face> sparseArray = faceDetector.detect(frame);

            detectFaces(sparseArray);

            imageView.setImageDrawable(new BitmapDrawable(getResources(), temporaryBitmap));

            faceDetector.release();
        }
    }

    private void initializeBitmap(BitmapFactory.Options bitmapOptions, Bitmap imageBitmap) {
        defaultBitmap = imageBitmap;//BitmapFactory.decodeFile(imgDecodableString);
        temporaryBitmap = imageBitmap;//Bitmap.createBitmap(defaultBitmap.getWidth(), defaultBitmap
                //.getHeight(), Bitmap.Config.RGB_565);
        eyePatchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eye_patch,
                bitmapOptions);
    }

    private void createRectanglePaint() {
        rectPaint = new Paint();
        rectPaint.setStrokeWidth(2);
        rectPaint.setColor(Color.CYAN);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    private void detectFaces(SparseArray<Face> sparseArray) {

        for (int i = 0; i < sparseArray.size(); i++) {
            Face face = sparseArray.valueAt(i);

            float left = face.getPosition().x;
            float top = face.getPosition().y;
            float right = left + face.getWidth();
            float bottom = right + face.getHeight();
            float cornerRadius = 2.0f;

            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, rectPaint);

            detectLandmarks(face);
        }
    }

    private void detectLandmarks(Face face) {
        for (Landmark landmark : face.getLandmarks()) {

            int cx = (int) (landmark.getPosition().x);
            int cy = (int) (landmark.getPosition().y);

            canvas.drawCircle(cx, cy, 2, rectPaint);

            //drawLandmarkType(landmark.getType(), cx, cy);

            //drawEyePatchBitmap(landmark.getType(), cx, cy);
        }
    }

    /*private void drawLandmarkType(int landmarkType, float cx, float cy) {
        String type = String.valueOf(landmarkType);
        rectPaint.setTextSize(5);
        canvas.drawText(type, cx, cy, rectPaint);
    }*/

    private void drawEyePatchBitmap(int landmarkType, float cx, float cy) {

        if (landmarkType == 4) {
            // TODO: Optimize so that this calculation is not done for every face
            int scaledWidth = eyePatchBitmap.getScaledWidth(canvas);
            int scaledHeight = eyePatchBitmap.getScaledHeight(canvas);
            canvas.drawBitmap(eyePatchBitmap, cx - (scaledWidth / 2), cy - (scaledHeight / 2), null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inMutable = true;

                initializeBitmap(bitmapOptions, imageBitmap);
                createRectanglePaint();
                processImage();
            }
            else {
                Toast.makeText(this, "You haven't took the Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}

package com.moyinoluwa.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Paint rectPaint;
    private Bitmap defaultBitmap;
    private Bitmap temporaryBitmap;
    private Bitmap eyePatchBitmap;
    private Canvas canvas;
    private Subscription faceDetectorSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (faceDetectorSubscription != null) {
            faceDetectorSubscription.unsubscribe();
        }
    }

    public void processImage(View view) {

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inMutable = true;

        initializeBitmap(bitmapOptions);
        createRectanglePaint();

        canvas = new Canvas(temporaryBitmap);
        canvas.drawBitmap(defaultBitmap, 0, 0, null);

        final FaceDetector faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face Detector could not be set up on your device :(")
                    .show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(defaultBitmap).build();

            faceDetectorSubscription = getSubscription(faceDetector, frame);
        }
    }

    private void initializeBitmap(BitmapFactory.Options bitmapOptions) {
        defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image,
                bitmapOptions);
        temporaryBitmap = Bitmap.createBitmap(defaultBitmap.getWidth(), defaultBitmap
                .getHeight(), Bitmap.Config.RGB_565);
        eyePatchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eye_patch,
                bitmapOptions);
    }

    private void createRectanglePaint() {
        rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
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

            canvas.drawCircle(cx, cy, 10, rectPaint);

            drawLandmarkType(landmark.getType(), cx, cy);

            drawEyePatchBitmap(landmark.getType(), cx, cy);
        }
    }

    private void drawLandmarkType(int landmarkType, float cx, float cy) {
        String type = String.valueOf(landmarkType);
        rectPaint.setTextSize(50);
        canvas.drawText(type, cx, cy, rectPaint);
    }

    private void drawEyePatchBitmap(int landmarkType, float cx, float cy) {

        if (landmarkType == 4) {
            // TODO: Optimize so that this calculation is not done for every face
            int scaledWidth = eyePatchBitmap.getScaledWidth(canvas);
            int scaledHeight = eyePatchBitmap.getScaledHeight(canvas);
            canvas.drawBitmap(eyePatchBitmap, cx - (scaledWidth / 2), cy - (scaledHeight / 2), null);
        }
    }

    private Observable<SparseArray> getFaceDetectorObservable(final FaceDetector faceDetector,
                                                              final Frame frame) {
        return Observable.fromCallable(new Callable<SparseArray>() {

            @Override
            public SparseArray call() throws Exception {
                SparseArray<Face> sparseArray = faceDetector.detect(frame);
                detectFaces(sparseArray);
                return null;
            }
        });
    }

    private Subscription getSubscription(final FaceDetector faceDetector, Frame frame) {
        return getFaceDetectorObservable(faceDetector, frame)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SparseArray>() {
                    @Override
                    public void onCompleted() {
                        imageView.setImageDrawable(new BitmapDrawable(getResources(),
                                temporaryBitmap));
                        faceDetector.release();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SparseArray sparseArray) {

                    }
                });
    }
}

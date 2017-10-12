package com.moyinoluwa.facedetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

private const val LEFT_EYE = 4
private const val RADIUS = 10f
private const val TEXT_SIZE = 50f
private const val CORNER_RADIUS = 2f
private const val STROKE_WIDTH = 5f
class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var defaultBitmap: Bitmap
    lateinit var temporaryBitmap: Bitmap
    lateinit var eyePatchBitmap: Bitmap
    lateinit var canvas: Canvas

    val rectPaint = Paint()
    val faceDetector: FaceDetector
        get() = initializeDetector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById<View>(R.id.image_view) as ImageView
    }

    fun processImage(view: View) {
        val bitmapOptions = BitmapFactory.Options().apply {
            inMutable = true
        }

        initializeBitmap(bitmapOptions)
        createRectanglePaint()

        canvas = Canvas(temporaryBitmap).apply {
            drawBitmap(defaultBitmap, 0f, 0f, null)
        }

        if (!faceDetector.isOperational) {
            AlertDialog.Builder(this)
                       .setMessage("Face Detector could not be set up on your device :(")
                       .show()
        } else {
            val frame = Frame.Builder().setBitmap(defaultBitmap).build()
            val sparseArray = faceDetector.detect(frame)

            detectFaces(sparseArray)

            imageView.setImageDrawable(BitmapDrawable(resources, temporaryBitmap))

            faceDetector.release()
        }
    }

    private fun initializeBitmap(bitmapOptions: BitmapFactory.Options) {
        defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.image, bitmapOptions)
        temporaryBitmap = Bitmap.createBitmap(defaultBitmap.width,
                                              defaultBitmap.height,
                                              Bitmap.Config.RGB_565)
        eyePatchBitmap = BitmapFactory.decodeResource(resources, R.drawable.eye_patch, bitmapOptions)
    }

    private fun createRectanglePaint() {
        rectPaint.apply {
            strokeWidth = STROKE_WIDTH
            color = Color.CYAN
            style = Paint.Style.STROKE
        }
    }

    private fun initializeDetector(): FaceDetector {
        return FaceDetector.Builder(this)
                           .setTrackingEnabled(false)
                           .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                           .build()
    }

    private fun detectFaces(sparseArray: SparseArray<Face>) {
        for (i in 0 until sparseArray.size()) {
            val face = sparseArray.valueAt(i)

            val left = face.position.x
            val top = face.position.y
            val right = left + face.width
            val bottom = top + face.height

            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, CORNER_RADIUS, CORNER_RADIUS, rectPaint)

            detectLandmarks(face)
        }
    }

    private fun detectLandmarks(face: Face) {
        for (landmark in face.landmarks) {
            val xCoordinate = landmark.position.x
            val yCoordinate = landmark.position.y

            canvas.drawCircle(xCoordinate, yCoordinate, RADIUS, rectPaint)

            drawLandmarkType(landmark.type, xCoordinate, yCoordinate)
            drawEyePatchBitmap(landmark.type, xCoordinate, yCoordinate)
        }
    }

    private fun drawLandmarkType(landmarkType: Int, xCoordinate: Float, yCoordinate: Float) {
        val type = landmarkType.toString()
        rectPaint.textSize = TEXT_SIZE
        canvas.drawText(type, xCoordinate, yCoordinate, rectPaint)
    }

    private fun drawEyePatchBitmap(landmarkType: Int, xCoordinate: Float, yCoordinate: Float) {
        when (landmarkType) {
            LEFT_EYE -> {
                // TODO: Optimize so that this calculation is not done for every face
                val scaledWidth = eyePatchBitmap.getScaledWidth(canvas)
                val scaledHeight = eyePatchBitmap.getScaledHeight(canvas)
                canvas.drawBitmap(eyePatchBitmap,
                                  xCoordinate - scaledWidth / 2,
                                  yCoordinate - scaledHeight / 2,
                                  null)
            }
        }
    }
}
package com.assessment.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.assessment.edgeviewer.camera.CameraManager
import com.assessment.edgeviewer.camera.FrameProcessor
import com.assessment.edgeviewer.gl.GLRenderer
import com.assessment.edgeviewer.utils.FPSCounter

class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    private val CAMERA_PERMISSION_CODE = 100
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private lateinit var statsTextView: TextView
    private lateinit var toggleButton: Button
    
    private var cameraManager: CameraManager? = null
    private var frameProcessor: FrameProcessor? = null
    private val fpsCounter = FPSCounter()
    
    private var currentMode = NativeProcessor.ProcessingMode.CANNY_EDGES
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        
        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun setupViews() {
        // GLSurfaceView setup
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glRenderer = GLRenderer()
        
        glSurfaceView.apply {
            setEGLContextClientVersion(2)
            setRenderer(glRenderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        
        // Stats TextView
        statsTextView = findViewById(R.id.statsTextView)
        updateStatsText(0.0, 0.0, 0, 0)
        
        // Toggle Button
        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener {
            toggleProcessingMode()
        }
    }
    
    private fun initializeCamera() {
        val width = 640
        val height = 480
        
        // Initialize frame processor
        frameProcessor = FrameProcessor(width, height) { bitmap, processingTime ->
            onFrameProcessed(bitmap, processingTime)
        }
        
        // Initialize camera manager
        cameraManager = CameraManager(this) { imageReader ->
            frameProcessor?.processFrame(imageReader)
        }
        
        cameraManager?.openCamera(width, height)
        
        Log.i(TAG, "Camera initialized")
    }
    
    private fun onFrameProcessed(bitmap: Bitmap, processingTime: Double) {
        fpsCounter.recordFrame()
        
        // Update OpenGL texture
        glSurfaceView.queueEvent {
            glRenderer.updateFrame(bitmap)
        }
        
        // Update stats UI
        runOnUiThread {
            val fps = fpsCounter.getFPS()
            val frameTime = fpsCounter.getAvgFrameTime()
            updateStatsText(fps, processingTime, bitmap.width, bitmap.height)
        }
    }
    
    private fun toggleProcessingMode() {
        currentMode = when (currentMode) {
            NativeProcessor.ProcessingMode.RAW -> NativeProcessor.ProcessingMode.GRAYSCALE
            NativeProcessor.ProcessingMode.GRAYSCALE -> NativeProcessor.ProcessingMode.CANNY_EDGES
            NativeProcessor.ProcessingMode.CANNY_EDGES -> NativeProcessor.ProcessingMode.RAW
        }
        
        frameProcessor?.setProcessingMode(currentMode)
        
        val modeName = when (currentMode) {
            NativeProcessor.ProcessingMode.RAW -> "Raw Feed"
            NativeProcessor.ProcessingMode.GRAYSCALE -> "Grayscale"
            NativeProcessor.ProcessingMode.CANNY_EDGES -> "Edge Detection"
        }
        
        Toast.makeText(this, "Mode: $modeName", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateStatsText(fps: Double, processingTime: Double, width: Int, height: Int) {
        val text = """
            FPS: ${"%.1f".format(fps)}
            Processing: ${"%.1f".format(processingTime)} ms
            Resolution: ${width}x${height}
            Mode: ${getModeString()}
        """.trimIndent()
        
        statsTextView.text = text
    }
    
    private fun getModeString(): String {
        return when (currentMode) {
            NativeProcessor.ProcessingMode.RAW -> "Raw"
            NativeProcessor.ProcessingMode.GRAYSCALE -> "Gray"
            NativeProcessor.ProcessingMode.CANNY_EDGES -> "Edges"
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.release()
        frameProcessor?.release()
        NativeProcessor.destroyProcessor()
    }
}

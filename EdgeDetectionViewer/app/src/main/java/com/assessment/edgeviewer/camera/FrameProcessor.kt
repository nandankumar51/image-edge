package com.assessment.edgeviewer.camera

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.media.ImageReader
import android.util.Log
import com.assessment.edgeviewer.NativeProcessor
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class FrameProcessor(
    private val width: Int,
    private val height: Int,
    private val onFrameProcessed: (Bitmap, Double) -> Unit
) {
    private val TAG = "FrameProcessor"
    
    private var processingMode = NativeProcessor.ProcessingMode.CANNY_EDGES
    private var isProcessing = false
    
    private val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    
    fun processFrame(imageReader: ImageReader) {
        if (isProcessing) {
            // Skip frame if still processing
            return
        }
        
        val image = imageReader.acquireLatestImage() ?: return
        isProcessing = true
        
        try {
            val startTime = System.nanoTime()
            
            // Convert Image to Bitmap
            val inputBitmap = imageToBitmap(image)
            
            if (inputBitmap != null) {
                // Process with OpenCV
                NativeProcessor.processFrame(inputBitmap, outputBitmap, processingMode)
                
                val totalTime = (System.nanoTime() - startTime) / 1_000_000.0
                
                // Callback with result
                onFrameProcessed(outputBitmap, totalTime)
                
                inputBitmap.recycle()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        } finally {
            image.close()
            isProcessing = false
        }
    }
    
    fun setProcessingMode(mode: NativeProcessor.ProcessingMode) {
        processingMode = mode
        Log.i(TAG, "Processing mode changed to: $mode")
    }
    
    fun getProcessingMode() = processingMode
    
    private fun imageToBitmap(image: Image): Bitmap? {
        return when (image.format) {
            ImageFormat.YUV_420_888 -> yuvToBitmap(image)
            else -> {
                Log.e(TAG, "Unsupported image format: ${image.format}")
                null
            }
        }
    }
    
    private fun yuvToBitmap(image: Image): Bitmap? {
        try {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            
            val nv21 = ByteArray(ySize + uSize + vSize)
            
            // Copy Y plane
            yBuffer.get(nv21, 0, ySize)
            
            // Copy UV planes (interleaved for NV21)
            val pixelStride = image.planes[1].pixelStride
            if (pixelStride == 1) {
                // Tightly packed, simple copy
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)
            } else {
                // Need to interleave
                val uvBuffer = ByteArray(uSize + vSize)
                vBuffer.get(uvBuffer, 0, vSize)
                uBuffer.get(uvBuffer, vSize, uSize)
                
                var pos = ySize
                for (i in 0 until (uSize + vSize) step pixelStride) {
                    nv21[pos++] = uvBuffer[i]
                }
            }
            
            // Convert NV21 to Bitmap
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
            val jpegData = out.toByteArray()
            
            return android.graphics.BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting YUV to Bitmap", e)
            return null
        }
    }
    
    fun release() {
        outputBitmap.recycle()
    }
}

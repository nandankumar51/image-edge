package com.assessment.edgeviewer

import android.graphics.Bitmap

object NativeProcessor {
    
    enum class ProcessingMode(val value: Int) {
        RAW(0),
        GRAYSCALE(1),
        CANNY_EDGES(2)
    }

    init {
        System.loadLibrary("native-lib")
        initProcessor()
    }

    /**
     * Initialize native processor
     */
    private external fun initProcessor()

    /**
     * Destroy native processor
     */
    external fun destroyProcessor()

    /**
     * Process a frame using OpenCV
     * @param inputBitmap Input bitmap (ARGB_8888)
     * @param outputBitmap Output bitmap (ARGB_8888, same size as input)
     * @param mode Processing mode
     */
    external fun processFrame(
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        mode: Int
    )

    /**
     * Get last processing time in milliseconds
     */
    external fun getLastProcessingTime(): Double

    /**
     * Helper method to process frame with enum
     */
    fun processFrame(
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        mode: ProcessingMode
    ) {
        processFrame(inputBitmap, outputBitmap, mode.value)
    }
}

package com.assessment.edgeviewer.utils

class FPSCounter(private val windowSize: Int = 30) {
    
    private val frameTimes = mutableListOf<Long>()
    private var lastFrameTime = 0L
    
    fun recordFrame() {
        val currentTime = System.nanoTime()
        
        if (lastFrameTime != 0L) {
            frameTimes.add(currentTime - lastFrameTime)
            
            // Keep only recent frames
            if (frameTimes.size > windowSize) {
                frameTimes.removeAt(0)
            }
        }
        
        lastFrameTime = currentTime
    }
    
    fun getFPS(): Double {
        if (frameTimes.isEmpty()) return 0.0
        
        val avgFrameTime = frameTimes.average()
        return 1_000_000_000.0 / avgFrameTime
    }
    
    fun getAvgFrameTime(): Double {
        if (frameTimes.isEmpty()) return 0.0
        return frameTimes.average() / 1_000_000.0 // Convert to ms
    }
    
    fun reset() {
        frameTimes.clear()
        lastFrameTime = 0L
    }
}

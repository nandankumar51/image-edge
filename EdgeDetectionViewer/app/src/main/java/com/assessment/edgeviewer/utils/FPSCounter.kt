package com.assessment.edgeviewer.utils

class FPSCounter {
    private var frames = 0
    private var start = System.currentTimeMillis()
    fun frame() {
        frames++
    }
}

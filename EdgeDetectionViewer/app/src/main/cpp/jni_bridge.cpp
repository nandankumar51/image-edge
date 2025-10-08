#include <jni.h>
#include <android/bitmap.h>
#include "opencv_processor.h"
#include <memory>

// Global processor instance
static std::unique_ptr<OpenCVProcessor> g_processor;

extern "C" {

JNIEXPORT void JNICALL
Java_com_assessment_edgeviewer_NativeProcessor_initProcessor(
    JNIEnv* env,
    jobject /* this */
) {
    g_processor = std::make_unique<OpenCVProcessor>();
    LOGI("Native processor initialized");
}

JNIEXPORT void JNICALL
Java_com_assessment_edgeviewer_NativeProcessor_destroyProcessor(
    JNIEnv* env,
    jobject /* this */
) {
    g_processor.reset();
    LOGI("Native processor destroyed");
}

JNIEXPORT void JNICALL
Java_com_assessment_edgeviewer_NativeProcessor_processFrame(
    JNIEnv* env,
    jobject /* this */,
    jobject inputBitmap,
    jobject outputBitmap,
    jint mode
) {
    if (!g_processor) {
        LOGE("Processor not initialized!");
        return;
    }

    AndroidBitmapInfo inputInfo;
    AndroidBitmapInfo outputInfo;
    void* inputPixels;
    void* outputPixels;

    // Get input bitmap info
    if (AndroidBitmap_getInfo(env, inputBitmap, &inputInfo) < 0) {
        LOGE("Failed to get input bitmap info");
        return;
    }

    // Get output bitmap info
    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo) < 0) {
        LOGE("Failed to get output bitmap info");
        return;
    }

    // Lock input bitmap
    if (AndroidBitmap_lockPixels(env, inputBitmap, &inputPixels) < 0) {
        LOGE("Failed to lock input bitmap");
        return;
    }

    // Lock output bitmap
    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels) < 0) {
        AndroidBitmap_unlockPixels(env, inputBitmap);
        LOGE("Failed to lock output bitmap");
        return;
    }

    // Process frame
    unsigned char* processedData = g_processor->processFrame(
        static_cast<unsigned char*>(inputPixels),
        inputInfo.width,
        inputInfo.height,
        static_cast<OpenCVProcessor::ProcessingMode>(mode)
    );

    // Copy to output bitmap
    std::memcpy(
        outputPixels,
        processedData,
        outputInfo.width * outputInfo.height * 4
    );

    // Cleanup
    delete[] processedData;
    AndroidBitmap_unlockPixels(env, inputBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}

JNIEXPORT jdouble JNICALL
Java_com_assessment_edgeviewer_NativeProcessor_getLastProcessingTime(
    JNIEnv* env,
    jobject /* this */
) {
    if (!g_processor) {
        return 0.0;
    }
    return g_processor->getLastProcessingTime();
}

} // extern "C"

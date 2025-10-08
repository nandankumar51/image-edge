#ifndef OPENCV_PROCESSOR_H
#define OPENCV_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "OpenCVProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class OpenCVProcessor {
public:
	enum ProcessingMode {
		MODE_RAW = 0,
		MODE_GRAYSCALE = 1,
		MODE_CANNY_EDGES = 2
	};

	OpenCVProcessor();
	~OpenCVProcessor();

	/**
	 * Process frame with specified mode
	 * @param inputData RGBA input frame data
	 * @param width Frame width
	 * @param height Frame height
	 * @param mode Processing mode
	 * @return Processed frame data (caller must delete[])
	 */
	unsigned char* processFrame(
		const unsigned char* inputData,
		int width,
		int height,
		ProcessingMode mode
	);

	/**
	 * Apply Canny edge detection
	 */
	cv::Mat applyCannyEdges(const cv::Mat& input);

	/**
	 * Convert to grayscale
	 */
	cv::Mat applyGrayscale(const cv::Mat& input);

	/**
	 * Get processing time in milliseconds
	 */
	double getLastProcessingTime() const { return lastProcessingTime; }

private:
	double lastProcessingTime;
	int cannyThreshold1;
	int cannyThreshold2;
};

#endif // OPENCV_PROCESSOR_H

cmake_minimum_required(VERSION 3.18.1)
project("edgeviewer")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV configuration
set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/../../../../opencv-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add native library
add_library(
	native-lib
	SHARED
	native-lib.cpp
	opencv_processor.cpp
	jni_bridge.cpp
)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# Link libraries
target_link_libraries(
	native-lib
	${OpenCV_LIBS}
	${log-lib}
	${android-lib}
	${jnigraphics-lib}
	GLESv2
	EGL
)

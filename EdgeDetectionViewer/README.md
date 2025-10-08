🧠 Android + OpenCV-C++ + OpenGL + Web (R&D Intern Assignment)

This project is a Real-Time Edge Detection Viewer built using Android (Kotlin), OpenCV (C++), OpenGL ES, JNI, and a TypeScript web viewer.

🚀 Features

📸 Live camera feed using Camera2 API

⚙️ Native OpenCV (C++) processing via JNI

🎨 Real-time rendering with OpenGL ES 2.0

🌐 Web viewer (TypeScript) displaying static processed image and FPS info

💾 Modular structure: /app, /jni, /gl, /web

⚙️ Tech Stack

Android: Kotlin / Java

Native: C++ (OpenCV, JNI)

Rendering: OpenGL ES 2.0

Web: TypeScript + HTML + CSS

🧠 Setup

Clone the repo

git clone https://github.com/yourusername/edge-detection-viewer.git


Open in Android Studio → install NDK & OpenCV SDK

Build & Run on a real device

For web:

cd web
tsc
open index.html

📁 Folder Structure
/app  → Android (Camera + UI)
/jni  → C++ (OpenCV Processing)
/gl   → OpenGL Rendering
/web  → TypeScript Web Viewer

🧱 Key Commits

feat: setup Android project with NDK and OpenCV

feat: implement camera feed

feat: add JNI + OpenCV edge detection

feat: render output via OpenGL

feat: add TypeScript web viewer

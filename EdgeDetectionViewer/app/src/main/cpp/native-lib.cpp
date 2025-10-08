#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_assessment_edgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string hello = "Hello from native";
    return env->NewStringUTF(hello.c_str());
}

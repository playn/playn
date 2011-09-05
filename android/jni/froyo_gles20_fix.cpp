#include <jni.h>
#include <GLES2/gl2.h>
#include "playn_android_AndroidNativeGL20.h"

//jobject or jclass?
extern "C" JNIEXPORT void JNICALL Java_playn_android_AndroidGL20Native_glDrawElements(
        JNIEnv *env, jobject c, jint mode, jint count, jint type, jint offset) {
    glDrawElements(mode, count, type, (void*) offset);
}

extern "C" JNIEXPORT void JNICALL Java_playn_android_AndroidGL20Native_glVertexAttribPointer(
		JNIEnv *env, jobject c, jint indx, jint size, jint type, jboolean normalized,
		jint stride, jint ptr) {
	glVertexAttribPointer(indx, size, type, normalized, stride, (void*) ptr);
}

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := playn-android
### Add all source file names to be included in lib separated by a whitespace
LOCAL_SRC_FILES := froyo_gles20_fix.cpp
LOCAL_LDLIBS    := -lGLESv2

include $(BUILD_SHARED_LIBRARY)

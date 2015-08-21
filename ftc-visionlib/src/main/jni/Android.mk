LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= ../opencv-build/sdk
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := libftcvision
LOCAL_SRC_FILES :=  detection.cpp \
                    video_homography.cpp \
                    image.cpp \
                    features.cpp \
                    featuredetection.cpp
LOCAL_LDLIBS +=  -llog -ldl
LOCAL_SHARED_LIBRARIES := opencv_java

include $(BUILD_SHARED_LIBRARY)

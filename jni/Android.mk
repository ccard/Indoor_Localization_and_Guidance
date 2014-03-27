LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include C:\NVPACK\OpenCV-2.4.5-Tegra-sdk-r2\sdk\native\jni\OpenCV.mk

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
LOCAL_MODULE:=LSH_Matcher

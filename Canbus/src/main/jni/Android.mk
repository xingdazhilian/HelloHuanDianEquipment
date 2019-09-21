LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Canbus
LOCAL_SRC_FILES := canbus.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS      := -O3 -fstrict-aliasing -ffast-math -funroll-loops -fpermissive
LOCAL_ARM_MODE    := arm

LOCAL_SRC_FILES   := addon.cpp

LOCAL_MODULE      := libaddon
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS      := -llog

include $(BUILD_SHARED_LIBRARY)

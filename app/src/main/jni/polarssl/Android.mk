LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

##### POLARSSL #####
DEBUG := true

##### DEBUG #####
$(info DEBUG set to $(DEBUG))
ifeq ($(DEBUG), true)
	NDK_DEBUG := 1
	DBGFLAGS := -ggdb -g3 -O0
	LOCAL_CFLAGS += $(DBGFLAGS)
endif

WARNING_CFLAGS := -Wall -W -Wdeclaration-after-statement
LOCAL_CFLAGS += $(WARNING_CFLAGS) -I$(LOCAL_PATH)/include -D_FILE_OFFSET_BITS=64 -std=c99

LOCAL_MODULE := polarssl
FILE_LIST := $(wildcard $(LOCAL_PATH)/library/*.c)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)
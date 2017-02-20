LOCAL_PATH := $(call my-dir)
temp := $(LOCAL_PATH)

##### POLARSSL #####
include $(CLEAR_VARS)

#LOCAL_MODULE    := libpolarssl
#LOCAL_SRC_FILES := libpolarssl.a
#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
#
#include $(PREBUILT_STATIC_LIBRARY)

include $(LOCAL_PATH)/polarssl/Android.mk

include $(CLEAR_VARS)
LOCAL_PATH := $(temp)

##### Proxenet #####

DEBUG := 1
DEBUG_SSL := 1

##### DEBUG #####
ifeq ($(DEBUG),1)
	NDK_DEBUG := 1

	DBGFLAGS := -ggdb -DDEBUG
	LOCAL_CFLAGS += $(DBGFLAGS)

	ifeq ($(DEBUG_SSL), 1)
		LOCAL_CFLAGS += -DDEBUG_SSL
	endif
endif

include $(CLEAR_VARS)

LOCAL_MODULE     := proxenet
FILE_LIST        := $(wildcard $(LOCAL_PATH)/source/*.c)
LOCAL_SRC_FILES  := $(FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_CFLAGS     += -I$(LOCAL_PATH)/polarssl/include -fPIC -Wall -std=c99 -D_C_PLUGIN
LOCAL_LDFLAGS    := -fPIC
LOCAL_LDLIBS     := -lc -ldl -pthread -llog
LOCAL_STATIC_LIBRARIES := libpolarssl

include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := testlib
LOCAL_SRC_FILES  := testlab/ndkfoo.c

LOCAL_CFLAGS := -fPIC -std=c99
LOCAL_LDFLAGS := -fPIC
LOCAL_LDLIBS := -lc -ldl -pthread -llog

LOCAL_STATIC_LIBRARIES := libpolarssl \
                            proxenet

include $(BUILD_SHARED_LIBRARY)

###########################################
##### ADDON
###########################################
include $(CLEAR_VARS)

LOCAL_MODULE            := addon
LOCAL_SRC_FILES         := Addon/addon.cpp
LOCAL_CFLAGS 			:= -fPIC -shared -fpermissive
LOCAL_LDFLAGS 			:= -fPIC
LOCAL_LDLIBS 			:= -llog

include $(BUILD_SHARED_LIBRARY)

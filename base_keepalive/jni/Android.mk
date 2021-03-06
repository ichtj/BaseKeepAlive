LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := leoric
LOCAL_SRC_FILES := leoric.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
TARGET_PLATFORM := android-3
LOCAL_MODULE := free-reflection
LOCAL_SRC_FILES := art.cpp main.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := daemon_api20
LOCAL_SRC_FILES := daemon_api20.c \
	common.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lm -lz
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := daemon_api21
LOCAL_SRC_FILES := daemon_api21.c \
	common.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lm -lz
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := daemon
LOCAL_SRC_FILES := daemon.c
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog -lm -lz
include $(BUILD_EXECUTABLE)
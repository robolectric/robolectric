LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_utils

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

include $(BUILD_HOST_JAVA_LIBRARY)
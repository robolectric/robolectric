LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_resources

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  Robolectric_utils \
  robolectric-guava-20.0 \
  jsr305lib

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

include $(BUILD_HOST_JAVA_LIBRARY)
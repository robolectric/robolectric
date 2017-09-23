LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_processor

LOCAL_CLASSPATH := $(HOST_JDK_TOOLS_JAR)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  robolectric-guava-20.0 \
  robolectric-gson-2.8 \
  jsr305lib

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_JAVA_RESOURCE_DIRS := src/main/resources

include $(BUILD_HOST_JAVA_LIBRARY)
##############################################
# Compile Robolectric resources
##############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_resources

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  Robolectric_utils \
  robolectric-guava-20.0 \
  jsr305lib

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Compile Robolectric resources tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_resources_tests

LOCAL_SRC_FILES := $(call all-java-files-under, src/test/java)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_resources \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Execute Robolectric resources tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Run_robolectric_resources_tests

test_source_directory := $(LOCAL_PATH)/src/test/java

test_runtime_libraries := \
  Robolectric_resources_tests \
  Robolectric_annotations \
  Robolectric_resources \
  Robolectric_utils \
  robolectric-byte-buddy-agent-1.6.5 \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-hamcrest-core-1.3 \
  robolectric-byte-buddy-1.6.5 \
  robolectric-objenesis-2.5 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12

include external/robolectric-shadows/run_robolectric_module_tests.mk
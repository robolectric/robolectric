##############################################
# Compile Robolectric shadows supportv4
##############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_shadows_supportv4

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_shadowapi \
  Robolectric_utils \
  robolectric-host-android-support-v4 \
  robolectric-host-android_all \
  robolectric-javax.annotation-api-1.2

LOCAL_ANNOTATION_PROCESSORS := \
  Robolectric_annotations \
  Robolectric_processor \
  robolectric-guava-20.0 \
  robolectric-gson-2.8

LOCAL_ANNOTATION_PROCESSOR_CLASSES := org.robolectric.annotation.processing.RobolectricProcessor

LOCAL_JAVACFLAGS := -Aorg.robolectric.annotation.processing.shadowPackage=org.robolectric.shadows.support.v4

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Compile Robolectric shadows supportv4 tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_shadows_supportv4_tests

LOCAL_SRC_FILES := $(call all-java-files-under, src/test/java)

LOCAL_JAVA_RESOURCE_DIRS := src/test/resources

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_supportv4 \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-accessibility-test-framework-2.1 \
  robolectric-host-android-support-v4 \
  robolectric-hamcrest-library-1.3 \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-hamcrest-core-1.3 \
  robolectric-host-android_all \
  robolectric-junit-4.12 \
  jsr305lib

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Execute Robolectric shadows supportv4 tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Run_robolectric_shadows_supportv4_tests

test_source_directory := $(LOCAL_PATH)/src/test/java

test_resources_directory := $(LOCAL_PATH)/src/test/resources

test_runtime_libraries := \
  Robolectric_shadows_supportv4_tests \
  Robolectric_shadows_supportv4 \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-accessibility-test-framework-2.1 \
  robolectric-host-monitor-1.0.2-alpha1 \
  robolectric-host-android-support-v4 \
  robolectric-byte-buddy-agent-1.6.5 \
  robolectric-maven-ant-tasks-2.1.3 \
  robolectric-hamcrest-library-1.3 \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-byte-buddy-1.6.5 \
  robolectric-host-android_all \
  robolectric-asm-commons-6.0 \
  robolectric-objenesis-2.5 \
  robolectric-asm-tree-6.0 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12 \
  robolectric-ant-1.8.0 \
  robolectric-asm-6.0 \
  jsr305lib

include external/robolectric-shadows/run_robolectric_module_tests.mk
################################################
# Compile Robolectric shadows androidx fragment
################################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_shadows_androidx_fragment

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_framework \
  Robolectric_shadowapi \
  robolectric-host-android_all \
  robolectric-host-androidx

LOCAL_ANNOTATION_PROCESSORS := \
  Robolectric_annotations \
  Robolectric_processor \
  robolectric-guava-25.1-jre \
  robolectric-gson-2.8

LOCAL_ANNOTATION_PROCESSOR_CLASSES := org.robolectric.annotation.processing.RobolectricProcessor

LOCAL_JAVACFLAGS := -Aorg.robolectric.annotation.processing.shadowPackage=org.robolectric.shadows.androidx.fragment

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

include $(BUILD_HOST_JAVA_LIBRARY)

######################################################
# Compile Robolectric shadows androidx fragment tests
######################################################
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_shadows_androidx_fragment_tests

LOCAL_SRC_FILES := $(call all-java-files-under, src/test/java)

LOCAL_JAVA_RESOURCE_DIRS := src/test/resources/res

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_androidx_fragment \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_utils \
  Robolectric_junit \
  robolectric-host-android_all \
  robolectric-guava-25.1-jre \
  robolectric-host-androidx \
  robolectric-junit-4.12 \
  robolectric-truth-0.42

include $(BUILD_HOST_JAVA_LIBRARY)

######################################################
# Execute Robolectric shadows androidx fragment tests
######################################################
include $(CLEAR_VARS)

LOCAL_MODULE := Run_robolectric_shadows_androidx_fragment_tests

test_source_directory := $(LOCAL_PATH)/src/test/java

test_resources_directory := $(LOCAL_PATH)/src/test/resources

test_runtime_libraries := \
  Robolectric_shadows_androidx_fragment \
  Robolectric_shadows_androidx_fragment_tests \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-hamcrest-library-1.3 \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-host-android_all \
  robolectric-asm-commons-6.0 \
  robolectric-guava-25.1-jre \
  robolectric-host-androidx \
  robolectric-objenesis-2.5 \
  robolectric-asm-tree-6.0 \
  robolectric-junit-4.12 \
  robolectric-truth-0.42 \
  robolectric-asm-6.0

include external/robolectric-shadows/run_robolectric_module_tests.mk
##############################################
# Compile Robolectric processor
##############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_processor

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_JAVA_RESOURCE_DIRS := src/main/resources

LOCAL_CLASSPATH := $(HOST_JDK_TOOLS_JAR)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  robolectric-guava-20.0 \
  robolectric-gson-2.8 \
  jsr305lib

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Compile Robolectric processor tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_processor_tests

LOCAL_SRC_FILES := $(call all-java-files-under, src/test/java)

LOCAL_JAVA_RESOURCE_DIRS := src/test/resources

LOCAL_JAVA_RESOURCE_FILES := $(LOCAL_PATH)/src/test/resources

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  Robolectric_processor \
  robolectric-compile-testing-0.12 \
  robolectric-mockito-core-2.7.6 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12 \
  robolectric-truth-0.36 \
  robolectric-gson-2.8 \
  jsr305lib

# Disable annotation processing while compiling tests to avoid executing RobolectricProcessor.
LOCAL_JAVACFLAGS := -proc:none

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Execute Robolectric processor tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Run_robolectric_processor_tests

test_source_directory := $(LOCAL_PATH)/src/test/java

test_runtime_libraries := \
  Robolectric_processor_tests \
  Robolectric_annotations \
  Robolectric_processor \
  Robolectric_shadowapi \
  robolectric-byte-buddy-agent-1.6.5 \
  robolectric-compile-testing-0.12 \
  robolectric-mockito-core-2.7.6 \
  robolectric-hamcrest-core-1.3 \
  robolectric-byte-buddy-1.6.5 \
  robolectric-objenesis-2.5 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12 \
  robolectric-truth-0.36 \
  robolectric-gson-2.8 \
  jsr305lib

include external/robolectric-shadows/run_robolectric_module_tests.mk
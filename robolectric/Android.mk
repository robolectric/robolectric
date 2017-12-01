##############################################
# Compile Robolectric robolectric
##############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_robolectric
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_IS_HOST_MODULE := true

intermediates := $(call local-intermediates-dir)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_shadowapi \
  Robolectric_resources \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-host-android_all \
  robolectric-host-monitor-1.0.2-alpha1 \
  robolectric-maven-ant-tasks-2.1.3 \
  robolectric-bouncycastle-1.46 \
  robolectric-asm-commons-6.0 \
  robolectric-xstream-1.4.8 \
  robolectric-asm-tree-6.0 \
  robolectric-junit-4.12 \
  robolectric-guava-20.0 \
  robolectric-ant-1.8.0 \
  robolectric-asm-6.0 \
  jsr305lib

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

$(intermediates)/robolectric-version.properties:
	$(hide) echo -n "robolectric.version=3.5-SNAPSHOT" >$@

LOCAL_JAVA_RESOURCE_FILES := $(intermediates)/robolectric-version.properties

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Compile Robolectric robolectric tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_robolectric_tests

LOCAL_SRC_FILES := $(call all-java-files-under, src/test/java)

LOCAL_JAVA_RESOURCE_DIRS := src/test/resources

LOCAL_JAVA_LIBRARIES := \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-host-monitor-1.0.2-alpha1 \
  robolectric-maven-ant-tasks-2.1.3 \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-sqlite4java-0.282 \
  robolectric-host-android_all \
  robolectric-guava-20.0 \
  robolectric-junit-4.12 \
  robolectric-ant-1.8.0 \
  jsr305lib

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Execute Robolectric robolectric tests
##############################################
include $(CLEAR_VARS)

LOCAL_MODULE := Run_robolectric_robolectric_tests

test_source_directory := $(LOCAL_PATH)/src/test/java

test_resources_directory := $(LOCAL_PATH)/src/test/resources

test_runtime_libraries := \
  Robolectric_robolectric_tests \
  Robolectric_shadows_framework \
  Robolectric_annotations \
  Robolectric_robolectric \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils \
  robolectric-host-monitor-1.0.2-alpha1 \
  robolectric-byte-buddy-agent-1.6.5 \
  robolectric-maven-ant-tasks-2.1.3 \
  robolectric-assertj-core-3.8.0 \
  robolectric-mockito-core-2.7.6 \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-sqlite4java-0.282 \
  robolectric-byte-buddy-1.6.5 \
  robolectric-host-android_all \
  robolectric-asm-commons-6.0 \
  robolectric-objenesis-2.5 \
  robolectric-xstream-1.4.8 \
  robolectric-asm-tree-6.0 \
  robolectric-guava-20.0 \
  robolectric-junit-4.12 \
  robolectric-icu4j-53.1 \
  robolectric-ant-1.8.0 \
  robolectric-asm-6.0 \
  jsr305lib

include external/robolectric-shadows/run_robolectric_module_tests.mk

###########################################
# HACK: specify these *TARGET* jars needed to compile robolectric as though they are prebuilt *HOST* java libraries
###########################################
LOCAL_PATH := $(LOCAL_PATH)/../../../
include $(CLEAR_VARS)

# Add this line to LOCAL_PREBUILT_JAVA_LIBRARIES below to compile robolectric against the latest SDK
# robolectric-host-android_all:$(call java-lib-files, robolectric_android-all)

# Add this line to LOCAL_PREBUILT_JAVA_LIBRARIES below to compile robolectric against the O SDK
# robolectric-host-android_all:prebuilts/misc/common/robolectric/android-all/android-all-o-preview-4-robolectric-0.jar

LOCAL_PREBUILT_JAVA_LIBRARIES := \
  robolectric-host-android_all:$(call intermediates-dir-for, JAVA_LIBRARIES, robolectric_android-all-stub,,COMMON)/classes-with-res.jar \
  robolectric-host-android-support-v4:$(call java-lib-files, android-support-v4) \
  robolectric-host-android-support-multidex:$(call java-lib-files, android-support-multidex) \
  robolectric-host-org_apache_http_legacy:$(call java-lib-files, org.apache.http.legacy) \
  robolectric-host-monitor-1.0.2-alpha1:$(call java-lib-files, robolectric-monitor-1.0.2-alpha1)

include $(BUILD_HOST_PREBUILT)

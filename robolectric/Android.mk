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
  robolectric-host-androidx-test \
  robolectric-bouncycastle-1.46 \
  robolectric-asm-commons-6.0 \
  robolectric-guava-25.1-jre \
  robolectric-xstream-1.4.8 \
  robolectric-asm-tree-6.0 \
  robolectric-junit-4.12 \
  robolectric-ant-1.8.0 \
  robolectric-asm-6.0 \
  jsr305

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

$(intermediates)/robolectric-version.properties:
	$(hide) echo -n "robolectric.version=3.7-SNAPSHOT" >$@

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
  robolectric-mockito-core-2.16.0 \
  robolectric-host-androidx-test \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-sqlite4java-0.282 \
  robolectric-host-android_all \
  robolectric-guava-25.1-jre \
  robolectric-junit-4.12 \
  robolectric-truth-0.42 \
  robolectric-ant-1.8.0 \
  jsr305

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
  robolectric-mockito-core-2.16.0 \
  robolectric-host-androidx-test \
  robolectric-bouncycastle-1.46 \
  robolectric-hamcrest-core-1.3 \
  robolectric-sqlite4java-0.282 \
  robolectric-byte-buddy-1.6.5 \
  robolectric-host-android_all \
  robolectric-asm-commons-6.0 \
  robolectric-diffutils-1.3.0 \
  robolectric-guava-25.1-jre \
  robolectric-objenesis-2.5 \
  robolectric-xstream-1.4.8 \
  robolectric-asm-tree-6.0 \
  robolectric-junit-4.12 \
  robolectric-icu4j-53.1 \
  robolectric-truth-0.42 \
  robolectric-ant-1.8.0 \
  robolectric-asm-6.0 \
  jsr305

include external/robolectric-shadows/run_robolectric_module_tests.mk

###########################################
# HACK: specify these *TARGET* jars needed to compile robolectric as though they are prebuilt *HOST* java libraries
###########################################
robolectric_target_to_host_jars := \
  robolectric-host-android_all:$(call intermediates-dir-for, JAVA_LIBRARIES, robolectric_android-all-stub,,COMMON)/classes-with-res.jar \
  robolectric-host-androidx-test:$(call java-lib-files, androidx.test.monitor) \
  robolectric-host-androidx:$(call java-lib-files, androidx.fragment_fragment) \
  robolectric-host-android-support-v4:$(call java-lib-files, android-support-v4) \
  robolectric-host-android-support-multidex:$(call java-lib-files, android-support-multidex) \
  robolectric-host-org_apache_http_legacy:$(call java-lib-files, org.apache.http.legacy.stubs) \
  robolectric-host-monitor-1.0.2-alpha1:$(call java-lib-files, robolectric-monitor-1.0.2-alpha1)

$(foreach p,$(robolectric_target_to_host_jars),\
  $(eval include $(CLEAR_VARS)) \
  $(eval LOCAL_MODULE := $(call word-colon,1,$(p))-prebuilt) \
  $(eval LOCAL_MODULE_CLASS := JAVA_LIBRARIES) \
  $(eval LOCAL_IS_HOST_MODULE := true) \
  $(eval LOCAL_PREBUILT_MODULE_FILE := $(call word-colon,2,$(p))) \
  $(eval include $(BUILD_PREBUILT)) \
  $(eval include $(CLEAR_VARS)) \
  $(eval LOCAL_MODULE := $(call word-colon,1,$(p))) \
  $(eval LOCAL_STATIC_JAVA_LIBRARIES := $(call word-colon,1,$(p))-prebuilt) \
  $(eval include $(BUILD_HOST_JAVA_LIBRARY)))

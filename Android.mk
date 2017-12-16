LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_all

LOCAL_STATIC_JAVA_LIBRARIES := \
  Robolectric_shadows_httpclient \
  Robolectric_shadows_framework \
  Robolectric_shadows_supportv4 \
  Robolectric_shadows_multidex \
  Robolectric_robolectric \
  Robolectric_annotations \
  Robolectric_resources \
  Robolectric_shadowapi \
  Robolectric_sandbox \
  Robolectric_junit \
  Robolectric_utils

LOCAL_JAVA_RESOURCE_DIRS := \
  shadows/framework/src/main/resources \
  src/main/resources

include $(BUILD_HOST_JAVA_LIBRARY)

include $(call first-makefiles-under, $(LOCAL_PATH))

###########################################
# target prebuilts for Robolectric
###########################################

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
  robolectric-monitor-1.0.2-alpha1:../../../prebuilts/tools/common/m2/repository/com/android/support/test/monitor/1.0.2-alpha1/monitor-1.0.2-alpha1.aar

include $(BUILD_MULTI_PREBUILT)

###########################################
# host prebuilts for Robolectric
###########################################

include $(CLEAR_VARS)
LOCAL_PREBUILT_JAVA_LIBRARIES := \
  robolectric-accessibility-test-framework-2.1:../../../prebuilts/tools/common/m2/repository/com/google/android/apps/common/testing/accessibility/framework/accessibility-test-framework/2.1/accessibility-test-framework-2.1.jar \
  robolectric-ant-1.8.0:../../../prebuilts/tools/common/m2/repository/org/apache/ant/ant/1.8.0/ant-1.8.0.jar \
  robolectric-asm-6.0:../../../prebuilts/tools/common/m2/repository/org/ow2/asm/asm/6.0/asm-6.0.jar \
  robolectric-asm-commons-6.0:../../../prebuilts/tools/common/m2/repository/org/ow2/asm/asm-commons/6.0/asm-commons-6.0.jar \
  robolectric-asm-tree-6.0:../../../prebuilts/tools/common/m2/repository/org/ow2/asm/asm-tree/6.0/asm-tree-6.0.jar \
  robolectric-bouncycastle-1.46:../../../prebuilts/tools/common/m2/repository/org/bouncycastle/bcprov-jdk16/1.46/bcprov-jdk16-1.46.jar \
  robolectric-gson-2.8:../../../prebuilts/tools/common/m2/repository/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar \
  robolectric-guava-20.0:../../../prebuilts/tools/common/m2/repository/com/google/guava/guava/20.0/guava-20.0.jar \
  robolectric-hamcrest-core-1.3:../../../prebuilts/tools/common/m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar \
  robolectric-hamcrest-library-1.3:../../../prebuilts/tools/common/m2/repository/org/hamcrest/hamcrest-library/1.3/hamcrest-library-1.3.jar \
  robolectric-httpclient-4.0.3:../../../prebuilts/tools/common/m2/repository/org/apache/httpcomponents/httpclient/4.0.3/httpclient-4.0.3.jar \
  robolectric-httpcore-4.0.1:../../../prebuilts/tools/common/m2/repository/org/apache/httpcomponents/httpcore/4.0.1/httpcore-4.0.1.jar \
  robolectric-junit-4.12:../../../prebuilts/tools/common/m2/repository/junit/junit/4.12/junit-4.12.jar \
  robolectric-maven-ant-tasks-2.1.3:../../../prebuilts/tools/common/m2/repository/org/apache/maven/maven-ant-tasks/2.1.3/maven-ant-tasks-2.1.3.jar \
  robolectric-sqlite4java-0.282:../../../prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/sqlite4java/0.282/sqlite4java-0.282.jar \
  robolectric-xstream-1.4.8:../../../prebuilts/tools/common/m2/repository/com/thoughtworks/xstream/xstream/1.4.8/xstream-1.4.8.jar

include $(BUILD_HOST_PREBUILT)
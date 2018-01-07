##############################################
# Compile Robolectric shadows framework
##############################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Robolectric_shadows_framework
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_IS_HOST_MODULE := true

intermediates := $(call local-intermediates-dir)

LOCAL_JAVA_LIBRARIES := \
  Robolectric_annotations \
  Robolectric_shadowapi \
  Robolectric_resources \
  Robolectric_utils \
  robolectric-accessibility-test-framework-2.1 \
  robolectric-hamcrest-library-1.3 \
  robolectric-hamcrest-core-1.3 \
  robolectric-sqlite4java-0.282 \
  robolectric-host-android_all \
  robolectric-guava-20.0 \
  icu4j-host \
  jsr305lib

LOCAL_JAVA_RESOURCE_FILES := \
  $(intermediates)/sqlite-natives:linux-x86_64/libsqlite4java.so \
  $(intermediates)/sqlite-natives:linux-x86/libsqlite4java.so \
  $(intermediates)/sqlite-natives:mac-x86_64/libsqlite4java.jnilib \
  $(intermediates)/sqlite-natives:windows-x86_64/sqlite4java.dll \
  $(intermediates)/sqlite-natives:windows-x86/sqlite4java.dll

LOCAL_ANNOTATION_PROCESSORS := \
  Robolectric_annotations \
  Robolectric_processor \
  robolectric-guava-20.0 \
  robolectric-gson-2.8

LOCAL_ANNOTATION_PROCESSOR_CLASSES := org.robolectric.annotation.processing.RobolectricProcessor

LOCAL_JAVACFLAGS := -Aorg.robolectric.annotation.processing.shadowPackage=org.robolectric

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_JAVA_RESOURCE_DIRS := src/main/resources

include $(BUILD_HOST_JAVA_LIBRARY)

##############################################
# Situate the SQLite native libraries
##############################################
$(intermediates)/sqlite-natives/linux-x86_64/libsqlite4java.so: prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/libsqlite4java-linux-amd64/0.282/libsqlite4java-linux-amd64-0.282.so
	$(hide) cp $< $@

$(intermediates)/sqlite-natives/linux-x86/libsqlite4java.so: prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/libsqlite4java-linux-i386/0.282/libsqlite4java-linux-i386-0.282.so
	$(hide) cp $< $@

$(intermediates)/sqlite-natives/mac-x86_64/libsqlite4java.jnilib: prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/libsqlite4java-osx/0.282/libsqlite4java-osx-0.282.jnilib
	$(hide) cp $< $@

$(intermediates)/sqlite-natives/windows-x86_64/sqlite4java.dll: prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/sqlite4java-win32-x64/0.282/sqlite4java-win32-x64-0.282.dll
	$(hide) cp $< $@

$(intermediates)/sqlite-natives/windows-x86/sqlite4java.dll: prebuilts/tools/common/m2/repository/com/almworks/sqlite4java/sqlite4java-win32-x86/0.282/sqlite4java-win32-x86-0.282.dll
	$(hide) cp $< $@
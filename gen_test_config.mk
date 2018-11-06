# Uses the following variables:
#
#   LOCAL_INSTRUMENTATION_FOR - specifies the name of the module that produces the APK under test
#

# Define a variable storing the location of the generated test_config.properties file.
test_config_dir := $(call local-intermediates-dir)/test_config
test_config_file := $(test_config_dir)/com/android/tools/test_config.properties

# Indicate that the generated file should be included in the output jar as a java resource.
LOCAL_JAVA_RESOURCE_FILES += $(test_config_dir):com/android/tools/test_config.properties

# Define variables to be written into the generated test_config.properties file.
android_merged_manifest := $(strip $(call intermediates-dir-for,APPS,$(LOCAL_INSTRUMENTATION_FOR),,COMMON)/manifest/AndroidManifest.xml)
android_resource_apk := $(strip $(call apk-location-for,$(LOCAL_INSTRUMENTATION_FOR)))

# Snapshot the written variables so they cannot be polluted before the module is built.
$(test_config_file): private_android_merged_manifest := $(android_merged_manifest)
$(test_config_file): private_android_resource_apk := $(android_resource_apk)

# Generate the test_config.properties file. Make it depend on the files to which it points.
$(test_config_file):$(android_merged_manifest) $(android_resource_apk)
	$(hide) rm -f $@
	$(hide) echo "android_merged_manifest=$(private_android_merged_manifest)" >>$@
	$(hide) echo "android_resource_apk=$(private_android_resource_apk)" >>$@
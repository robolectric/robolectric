LOCAL_MODULE_CLASS := FAKE
LOCAL_IS_HOST_MODULE := true
LOCAL_DONT_CHECK_MODULE := true
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_BUILT_MODULE_STEM := test.fake

# Construct the runtime classpath.
classpath_jars := $(call java-lib-files, $(test_runtime_libraries), HOST)

# Construct the list of test classes from the source file names.
test_source_files := $(call find-files-in-subdirs, $(test_source_directory), "*Test.java", .)
# Filter out tests that will not pass running under make.
test_source_files := $(filter-out org/robolectric/internal/GradleManifestFactoryTest.java, $(test_source_files))

# Convert the test source file paths into package names by removing ".java" extension and replacing "/" with "."
test_class_names := $(subst /,., $(basename $(test_source_files)))
# Remove whitespace and sort the tests in alphabetical order.
test_class_names := $(sort $(shell echo '$(test_class_names)' | tr ' ' '\n'))

include $(BUILD_SYSTEM)/base_rules.mk

# Define rules that copy test resource files to the intermediates folder.
intermediates := $(call local-intermediates-dir)
copy_test_resource_files :=
ifdef test_resources_directory
  test_resources_target_path := $(intermediates)/src/test/resources
  test_resource_files := $(call find-files-in-subdirs, $(test_resources_directory), "*" -and -type f, .)
  copy_test_resource_file_pairs := $(foreach j, $(test_resource_files), $(test_resources_directory)/$(j):$(test_resources_target_path)/$(j))
  copy_test_resource_files := $(call copy-many-files, $(copy_test_resource_file_pairs))
endif

# Define rules that copy android-all jars to the intermediates folder.
p_android_all_source_jar := $(call intermediates-dir-for, JAVA_LIBRARIES, robolectric_android-all-stub, , COMMON)/classes-with-res.jar
android_all_source_dir := prebuilts/misc/common/robolectric/android-all
android_all_target_dir := $(intermediates)/android-all
copy_android_all_jar_pairs := \
  $(android_all_source_dir)/android-all-4.1.2_r1-robolectric-r1.jar:$(android_all_target_dir)/android-all-4.1.2_r1-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-4.2.2_r1.2-robolectric-r1.jar:$(android_all_target_dir)/android-all-4.2.2_r1.2-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-4.3_r2-robolectric-r1.jar:$(android_all_target_dir)/android-all-4.3_r2-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-4.4_r1-robolectric-r2.jar:$(android_all_target_dir)/android-all-4.4_r1-robolectric-r2.jar \
  $(android_all_source_dir)/android-all-5.0.2_r3-robolectric-r0.jar:$(android_all_target_dir)/android-all-5.0.2_r3-robolectric-r0.jar \
  $(android_all_source_dir)/android-all-5.1.1_r9-robolectric-r2.jar:$(android_all_target_dir)/android-all-5.1.1_r9-robolectric-r2.jar \
  $(android_all_source_dir)/android-all-6.0.1_r3-robolectric-r1.jar:$(android_all_target_dir)/android-all-6.0.1_r3-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-7.0.0_r1-robolectric-r1.jar:$(android_all_target_dir)/android-all-7.0.0_r1-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-7.1.0_r7-robolectric-r1.jar:$(android_all_target_dir)/android-all-7.1.0_r7-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-8.0.0_r4-robolectric-r1.jar:$(android_all_target_dir)/android-all-8.0.0_r4-robolectric-r1.jar \
  $(android_all_source_dir)/android-all-8.1.0-robolectric-r4458339.jar:$(android_all_target_dir)/android-all-8.1.0-robolectric-r4458339.jar \
  $(p_android_all_source_jar):$(android_all_target_dir)/android-all-P-robolectric-r0.jar
copy_android_all_jars := $(call copy-many-files, $(copy_android_all_jar_pairs))

# Snapshot the variables so they cannot be polluted before the module is built.
$(LOCAL_BUILT_MODULE): private_java := $(JAVA)
$(LOCAL_BUILT_MODULE): private_working_directory := $(intermediates)
$(LOCAL_BUILT_MODULE): private_test_class_names := $(test_class_names)
$(LOCAL_BUILT_MODULE): private_host_jdk_tools_jar := $(HOST_JDK_TOOLS_JAR)
$(LOCAL_BUILT_MODULE): private_android_all_dir := $(android_all_target_dir)
$(LOCAL_BUILT_MODULE): private_classpath_jars := $(call normalize-path-list, $(classpath_jars))

.PHONY: $(LOCAL_BUILT_MODULE)

# Define the basic recipe for building this module to execute the tests.
$(LOCAL_BUILT_MODULE): $(copy_test_resource_files) $(copy_android_all_jars) $(classpath_jars)
	$(hide) $(private_java) \
	  -Drobolectric.offline=true \
	  -Drobolectric.dependency.dir=$(private_android_all_dir) \
	  -Drobolectric-tests.base-dir=$(private_working_directory) \
	  -cp $(private_host_jdk_tools_jar):$(private_working_directory):$(private_classpath_jars) \
	  org.junit.runner.JUnitCore \
	  $(private_test_class_names)
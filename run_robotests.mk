# Rules for running robolectric tests.
#
# Uses the following variables:
#
#   LOCAL_JAVA_LIBRARIES
#   LOCAL_STATIC_JAVA_LIBRARIES
#   LOCAL_ROBOTEST_FAILURE_FATAL
#   LOCAL_ROBOTEST_TIMEOUT
#   LOCAL_TEST_PACKAGE
#   LOCAL_ROBOTEST_FILES
#   ROBOTEST_FAILURE_FATAL
#   ROBOTEST_FILTER
#   ROBOTEST_RUN_INDIVIDUALLY
#
#
# If ROBOTEST_FAILURE_FATAL is set to true then failing tests will cause a
# build failure. Otherwise failures will be logged but ignored by make.
#
# If ROBOTEST_FILTER is set to a regex then only tests matching that pattern
# will be run. This currently only works at the class level.
#
# TODO: Switch to a JUnit runner which can support method-level test
# filtering and use that rather than grep to implement ROBOTEST_FILTER.
#
# If ROBOTEST_RUN_INDIVIDUALLY is set to true, each test class will be run by a
# different JVM, preventing any interaction between different tests. This is
# significantly slower than running all tests within the same JVM, but prevents
# unwanted interactions.
#
# Tests classes are found by looking for *Test.java files in
# LOCAL_PATH recursively.

################################################
# General settings, independent of the module. #
################################################

### Used for running tests.

# Where to find Robolectric.
my_robolectric_script_path := $(call my-dir)

my_collect_target := $(LOCAL_MODULE)-coverage
my_report_target := $(LOCAL_MODULE)-jacoco
# Whether or not to ignore the result of running the robotests.
# LOCAL_ROBOTEST_FAILURE_FATAL will take precedence over ROBOTEST_FAILURE_FATAL,
# if present.
my_failure_fatal := $(if $(LOCAL_ROBOTEST_FAILURE_FATAL)$(ROBOTEST_FAILURE_FATAL),true,false)
# The timeout for the command. A value of '0' means no timeout. The default is
# 10 minutes.
my_timeout := $(if $(LOCAL_ROBOTEST_TIMEOUT),$(LOCAL_ROBOTEST_TIMEOUT),600)
# Command to filter the list of test classes.
# If not specified, defaults to including all the tests.
my_test_filter_command := $(if $(ROBOTEST_FILTER),grep -E "$(ROBOTEST_FILTER)",cat)

# The directory containing the sources.
my_instrument_makefile_dir := $(dir $(ALL_MODULES.$(LOCAL_TEST_PACKAGE).MAKEFILE))
my_instrument_source_dirs := $(if $(LOCAL_INSTRUMENT_SOURCE_DIRS),\
    $(LOCAL_INSTRUMENT_SOURCE_DIRS),\
    $(my_instrument_makefile_dir)src $(my_instrument_makefile_dir)java)

##########################
# Used by base_rules.mk. #
##########################

LOCAL_MODULE_CLASS := ROBOLECTRIC
# This is actually a phony target that is never built.
LOCAL_BUILT_MODULE_STEM := test.fake
# Since it is not built, it cannot be installed. But we will define our own
# dist files, depending on which of the specific targets is invoked.
LOCAL_UNINSTALLABLE_MODULE := true
# Do not build it for checkbuild or mma
LOCAL_DONT_CHECK_MODULE := true

include $(BUILD_SYSTEM)/base_rules.mk


#############################
# Module specific settings. #
#############################

### Used for running tests.

# The list of test classes. Robolectric requires an explicit list of tests to
# run, which is compiled from the Java files ending in "Test" within the
# directory from which this module is invoked.
ifeq ($(strip $(LOCAL_ROBOTEST_FILES)),)
    LOCAL_ROBOTEST_FILES := $(call find-files-in-subdirs,$(LOCAL_PATH)/src,*Test.java,.)
endif
# Convert the paths into package names by removing .java extension and replacing "/" with "."
my_tests := $(subst /,.,$(basename $(LOCAL_ROBOTEST_FILES)))
my_tests := $(sort $(shell echo '$(my_tests)' | tr ' ' '\n' | $(my_test_filter_command)))
# The source jars containing the tests.
my_srcs_jars := \
    $(foreach lib, \
        $(LOCAL_JAVA_LIBRARIES) $(LOCAL_STATIC_JAVA_LIBRARIES), \
        $(call intermediates-dir-for,JAVA_LIBRARIES,$(lib),,COMMON)/classes-pre-proguard.jar) \
    $(foreach lib, \
        $(LOCAL_TEST_PACKAGE), \
        $(call intermediates-dir-for,APPS,$(lib),,COMMON)/classes-pre-proguard.jar)
# The jars needed to run the tests.
my_jars := \
    $(my_robolectric_jars) \
    $(call java-lib-files,junitxml) \
    $(my_srcs_jars)



# Run tests.
my_target := $(LOCAL_BUILT_MODULE)
my_filename_stem := test

# Define rules that copy android-all jars to the intermediates folder.
p_android_all_source_jar := $(call intermediates-dir-for, JAVA_LIBRARIES, robolectric_android-all-stub, , COMMON)/classes-with-res.jar
android_all_lib_path := prebuilts/misc/common/robolectric/android-all
my_robolectric_path := $(intermediates.COMMON)/android-all
copy_android_all_jar_pairs := \
  $(android_all_lib_path)/android-all-4.1.2_r1-robolectric-r1.jar:$(my_robolectric_path)/android-all-4.1.2_r1-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-4.2.2_r1.2-robolectric-r1.jar:$(my_robolectric_path)/android-all-4.2.2_r1.2-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-4.3_r2-robolectric-r1.jar:$(my_robolectric_path)/android-all-4.3_r2-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-4.4_r1-robolectric-r2.jar:$(my_robolectric_path)/android-all-4.4_r1-robolectric-r2.jar \
  $(android_all_lib_path)/android-all-5.0.2_r3-robolectric-r0.jar:$(my_robolectric_path)/android-all-5.0.2_r3-robolectric-r0.jar \
  $(android_all_lib_path)/android-all-5.1.1_r9-robolectric-r2.jar:$(my_robolectric_path)/android-all-5.1.1_r9-robolectric-r2.jar \
  $(android_all_lib_path)/android-all-6.0.1_r3-robolectric-r1.jar:$(my_robolectric_path)/android-all-6.0.1_r3-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-7.0.0_r1-robolectric-r1.jar:$(my_robolectric_path)/android-all-7.0.0_r1-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-7.1.0_r7-robolectric-r1.jar:$(my_robolectric_path)/android-all-7.1.0_r7-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-8.0.0_r4-robolectric-r1.jar:$(my_robolectric_path)/android-all-8.0.0_r4-robolectric-r1.jar \
  $(android_all_lib_path)/android-all-8.1.0-robolectric-r4458339.jar:$(my_robolectric_path)/android-all-8.1.0-robolectric-r4458339.jar \
  $(p_android_all_source_jar):$(my_robolectric_path)/android-all-P-robolectric-r0.jar
copy_android_all_jars := $(call copy-many-files, $(copy_android_all_jar_pairs))

$(my_target): $(copy_android_all_jars)

include $(my_robolectric_script_path)/robotest-internal.mk
# clean local variables
my_java_args :=
my_target :=

# Target for running robolectric tests using jacoco
my_target := $(LOCAL_BUILT_MODULE)-coverage
my_filename_stem := coverage
$(my_collect_target): $(my_target)
$(my_target): $(call java-lib-files,jvm-jacoco-agent,true) $(copy_android_all_jars)

my_coverage_dir := $(intermediates)/coverage
my_coverage_file := $(my_coverage_dir)/jacoco.exec

# List of packages to exclude jacoco from running
my_jacoco_excludes := \
    org.robolectric.* \
    org.mockito.* \
    org.junit.* \
    org.objectweb.* \
    com.thoughtworks.xstream.*
# The Jacoco agent JAR.
my_jacoco_agent_jar := $(call java-lib-files,jvm-jacoco-agent,true)
# Using Jacoco with Robolectric is broken in 0.7.3 <= version < 0.7.6.
# In 0.7.6 or above, the parameter "inclnolocationclasses" is needed.
# See https://github.com/jacoco/jacoco/pull/288 for more
# In JDK9, if "inclnolocationclasses" is used, we also need to specify
# exclclassloader=jdk.internal.reflect.DelegatingClassLoader
# https://github.com/jacoco/jacoco/issues/16
my_jacoco_agent_args = \
    destfile=$(my_coverage_file) \
    excludes=$(call normalize-path-list, $(my_jacoco_excludes)) \
    inclnolocationclasses=true \
    exclclassloader=jdk.internal.reflect.DelegatingClassLoader \
    append=false
my_java_args := \
    -javaagent:$(my_jacoco_agent_jar)=$(call normalize-comma-list, $(my_jacoco_agent_args))
include $(my_robolectric_script_path)/robotest-internal.mk
# Clear temporary variables
my_failure_fatal :=
my_jacoco_agent_jar :=
my_jacoco_agent_args :=
my_jacoco_excludes :=
my_java_args :=
my_robolectric_jars :=
my_target :=
my_tests :=
my_filename_stem :=

# Target for generating code coverage reports using jacoco.exec
my_target := $(LOCAL_BUILT_MODULE)-jacoco
$(my_report_target): $(my_target)

# The JAR file containing the report generation tool.
my_coverage_report_class := com.google.android.jacoco.reporter.ReportGenerator
my_coverage_report_jar := $(call java-lib-files,jvm-jacoco-reporter,true)
my_coverage_srcs_jars := $(my_srcs_jars)
my_coverage_report_dist_file := $(my_report_target)-html.zip

## jacoco code coverage reports
include $(my_robolectric_script_path)/report-internal.mk
# Clear temporary variables
my_coverage_dir :=
my_coverage_file :=
my_coverage_report_class :=
my_coverage_report_dist_file :=
my_coverage_report_jar :=
my_coverage_srcs_jars :=
my_robolectric_script_path :=
my_robolectric_path :=
my_srcs_jars :=
my_target :=

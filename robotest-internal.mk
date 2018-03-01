# Defines a target named $(my_target) for running robolectric tests.

# Running the tests is done in two stages: we first generate the test output to
# $(my_target_output), which is also added to the dist list, and store the
# return value of running the tests in $(my_target_retval). After that we
# process the output and return value as part of $(my_target). This is needed
# to make sure that we can install the test output even if the tests actually
# fail.

# Files in which to store the output and return value of the tests.
my_target_xml := $(intermediates)/$(my_filename_stem)-output.xml
my_target_output := $(intermediates)/$(my_filename_stem)-output.txt
my_target_retval := $(intermediates)/$(my_filename_stem)-retval.txt

# We should always re-run the tests, even if nothing has changed.
.PHONY: $(my_target_output) $(my_target_retval)
# Private variables.
$(my_target_output): PRIVATE_MODULE := $(LOCAL_MODULE)
$(my_target_output): PRIVATE_TESTS := $(my_tests)
$(my_target_output): PRIVATE_JARS := $(my_jars)
$(my_target_output): PRIVATE_JAVA_ARGS := $(my_java_args)
$(my_target_output): PRIVATE_ROBOLECTRIC_PATH := $(my_robolectric_path)
$(my_target_output): PRIVATE_ROBOLECTRIC_SCRIPT_PATH := $(my_robolectric_script_path)
$(my_target_output): PRIVATE_TARGET_MESSAGE := $(my_target_message)
$(my_target_output): PRIVATE_TARGET_OUTPUT := $(my_target_output)
$(my_target_output): PRIVATE_TARGET_RETVAL := $(my_target_retval)
$(my_target_output): PRIVATE_TIMEOUT := $(my_timeout)
$(my_target_output): PRIVATE_XML_OUTPUT_FILE := $(my_target_xml)
# Runs the Robolectric tests and saves the output and return value.
$(my_target_output): $(my_jars)
	@echo "host Robolectric: $(PRIVATE_MODULE)"
	# Run `touch` to always create the output XML file, so the build doesn't break even if the
	# runner failed to create the XML output
	$(hide) touch "$(PRIVATE_XML_OUTPUT_FILE)"
	$(hide) \
	  PRIVATE_INTERMEDIATES="$(dir $@)" \
	  PRIVATE_JARS="$(PRIVATE_JARS)" \
	  PRIVATE_JAVA_ARGS="$(PRIVATE_JAVA_ARGS)" \
	  PRIVATE_ROBOLECTRIC_PATH="$(PRIVATE_ROBOLECTRIC_PATH)" \
	  PRIVATE_ROBOLECTRIC_SCRIPT_PATH="$(PRIVATE_ROBOLECTRIC_SCRIPT_PATH)" \
	  PRIVATE_RUN_INDIVIDUALLY="$(ROBOTEST_RUN_INDIVIDUALLY)" \
	  PRIVATE_TARGET_MESSAGE="$(PRIVATE_TARGET_MESSAGE)" \
	  PRIVATE_TIMEOUT="$(PRIVATE_TIMEOUT)" \
	  PRIVATE_TESTS="$(PRIVATE_TESTS)" \
	  XML_OUTPUT_FILE="$(PRIVATE_XML_OUTPUT_FILE)" \
	  TEST_WORKSPACE="$(PRIVATE_MODULE)" \
	  $(PRIVATE_ROBOLECTRIC_SCRIPT_PATH)/wrapper.sh \
	    "$(PRIVATE_MODULE)" \
	    "$(PRIVATE_TARGET_OUTPUT)" \
	    "$(PRIVATE_TARGET_RETVAL)" \
	    wrap \
	    $(PRIVATE_ROBOLECTRIC_SCRIPT_PATH)/robotest.sh

$(my_target_xml) $(my_target_retval): $(my_target_output)

# This does not actually generate a file.
.PHONY: $(my_target)
# Private variables.
$(my_target): PRIVATE_MODULE := $(LOCAL_MODULE)
$(my_target): PRIVATE_TARGET_OUTPUT := $(my_target_output)
$(my_target): PRIVATE_TARGET_RETVAL := $(my_target_retval)
$(my_target): PRIVATE_FAILURE_FATAL := $(my_failure_fatal)
$(my_target): PRIVATE_ROBOLECTRIC_SCRIPT_PATH := $(my_robolectric_script_path)
# Process the output and the return value of the tests. This will fail if the
# return value is non-zero.
$(my_target): $(my_target_output) $(my_target_xml)
	$(hide) \
	  result=0; \
	  $(PRIVATE_ROBOLECTRIC_SCRIPT_PATH)/wrapper.sh \
	    "$(PRIVATE_MODULE)" \
	    "$(PRIVATE_TARGET_OUTPUT)" \
	    "$(PRIVATE_TARGET_RETVAL)" \
	    eval \
	      || result=$$?; \
	  if [ "$(strip $(PRIVATE_FAILURE_FATAL))" = true ]; then \
	    exit "$$result"; \
	  fi

# Add the output of the tests to the dist list, so that we will include it even
# if the tests fail.
$(call dist-for-goals, $(my_target), \
    $(my_target_output):robotests/$(LOCAL_MODULE)-$(notdir $(my_target_output)) \
    $(my_target_xml):robotests/$(LOCAL_MODULE)-$(notdir $(my_target_xml)))

# Clean up local variables.
my_target_output :=
my_target_retval :=
my_target_xml :=
my_filename_stem :=

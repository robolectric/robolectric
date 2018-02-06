#!/bin/bash
#
# A simple script to wrap the execution of a command so that it stores its
# output and return value into a file and then process it to later on.
#
# This is meant to be used in a makefile, specifically to allow for the output
# of a command to be stored in a file and added to the dist list, even if the
# command actually failed.
#
# For example, your makefile might include:
#
# my_target := lint
# my_target_output := $(OUT_DIR)/lint-output.txt
# my_target_retval := $(OUT_DIR)/lint-retval.txt
#
# $(my_target_output) $(my_target_retval): PRIVATE_MODULE := $(my_target)
# $(my_target_output) $(my_target_retval): PRIVATE_OUTPUT := $(my_target_output)
# $(my_target_output) $(my_target_retval): PRIVATE_RETVAL := $(my_target_retval)
# $(my_target_output) $(my_target_retval):
#         $(PATH)/wrapper.sh \
#           $(PRIVATE_MODULE) \
#           $(PRIVATE_OUTPUT) \
#           $(PRIVATE_RETVAL) \
#           wrap \
#           $(PATH)/run-list.sh $(LOCAL_PATH)/src
#
# $(my_target): PRIVATE_MODULE := $(my_target)
# $(my_target): PRIVATE_OUTPUT := $(my_target_output)
# $(my_target): PRIVATE_RETVAL := $(my_target_retval)
# $(my_target): $(my_target_output) $(my_target_retval)
#         $(PATH)/wrapper.sh \
#           $(PRIVATE_MODULE) \
#           $(PRIVATE_OUTPUT) \
#           $(PRIVATE_RETVAL) \
#           eval

set -euo pipefail

# Terminate with a fatal error.
function fatal() {
  echo "Fatal: $*"
  exit 113
}

function main() {
  local module="${1-}"; shift || fatal "missing argument: module"
  local output="${1-}"; shift || fatal "missing argument: output"
  local retval="${1-}"; shift || fatal "missing argument: retval"
  local action="${1-}"; shift || fatal "missing argument: action"
  # The rest of the arguments are the command to run.

  if [ "$action" = 'wrap' ]; then
    # Run the command specified by the rest of arguments ("$@") and save output
    # and return value.
    echo 0 >"${retval}"
    "$@" >"${output}" 2>&1 || echo "$?" >"${retval}"

    # Wrapping itself is always successful.
    return
  elif [ "$action" = 'eval' ]; then
    local result="$(cat "${retval}")"
    if [ "$result" = 0 ]; then
      # If successful only print the last few lines.
      tail -n 5 "$output" | sed -e "s/^/${module}: /"
    else
      # Print the entire output on failure.
      cat "$output" | sed -e "s/^/${module}: /"
    fi
    # Evaluating returns the stored return value.
    return "$result"
  else
    fatal "invalid action: $action"
  fi
}

main "$@"

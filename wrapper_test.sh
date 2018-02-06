#!/bin/bash
#
# Tests for wrapper.sh.

set -euo pipefail

# The location of the script under test.
readonly WRAPPER="$(realpath "$(dirname "$0")/wrapper.sh")"
# The name of the tests to run. Each test correspond to a function in this file
# whose name is the name of the test prefixed by 'test'.
readonly TEST_NAMES=(
  SuccessfulCase
  FailedCase
  FailedSignalCase
)

# Fails with an error message.
function fatal() {
  echo 1>&2 "FATAL: $@"
  exit 113
}

function withTestFiles() {
  (
    echo '1'
    echo '2'
    echo '3'
    echo '4'
    echo '5'
    echo '6'
    echo '7'
  ) >testfile
  (
    echo 'module: 1'
    echo 'module: 2'
    echo 'module: 3'
    echo 'module: 4'
    echo 'module: 5'
    echo 'module: 6'
    echo 'module: 7'
  ) >testfileWithModule
  (
    echo 'module: 3'
    echo 'module: 4'
    echo 'module: 5'
    echo 'module: 6'
    echo 'module: 7'
  ) >testfileWithModuleTruncated
}

function expectNoWrapOutput() {
  if [ "$(cat output-wrap)" != '' ]; then
    echo 'Wrap should not generate any output'
    diff testfile output || true
    return 1
  fi
}

function expectSavedOutput() {
  if ! diff testfile output; then
    echo 'Should have saved the correct output'
    diff testfile output || true
    return 1
  fi
}

function expectFullOutputWithModule() {
  if ! diff testfileWithModule output-eval; then
    echo 'Should have printed the full output'
    diff testfileWithModule output || true
    return 1
  fi
}

function expectTruncatedOutputWithModule() {
  if ! diff testfileWithModuleTruncated output-eval; then
    echo 'Should have printed the truncated output'
    diff testfileWithModuleTruncated output || true
    return 1
  fi
}

function whenWrap() {
  "$WRAPPER" module "$PWD/output" "$PWD/retval" 'wrap' "$@" \
    2>/dev/null \
    >output-wrap
}

function whenEval() {
  "$WRAPPER" module "$PWD/output" "$PWD/retval" 'eval' "$@" \
    >output-eval 2>&1
}

function testSuccessfulCase() {
  withTestFiles
  (
    echo '#!/bin/bash'
    echo
    echo 'cat testfile'
  ) >script.sh
  chmod 755 script.sh
  whenWrap "$PWD/script.sh"
  expectNoWrapOutput
  if ! whenEval; then
    echo 'Should have run successfully'
    return 1
  fi
  expectSavedOutput
  expectTruncatedOutputWithModule
}

function testFailedCase() {
  withTestFiles
  (
    echo '#!/bin/bash'
    echo
    echo 'cat testfile'
    echo 'exit 1'
  ) >script.sh
  chmod 755 script.sh
  whenWrap "$PWD/script.sh"
  expectNoWrapOutput
  if whenEval; then
    echo 'Should have failed to run'
    return 1
  fi
  expectSavedOutput
  expectFullOutputWithModule
}

function testFailedSignalCase() {
  withTestFiles
  (
    echo '#!/bin/bash'
    echo
    echo 'cat testfile'
    echo 'kill -TERM $$'
    echo 'echo Should not be printed'
  ) >script.sh
  chmod 755 script.sh
  whenWrap "$PWD/script.sh"
  expectNoWrapOutput
  if whenEval; then
    echo 'Should have failed to run'
    return 1
  fi
  expectSavedOutput
  expectFullOutputWithModule
}

function main() {
  local result=0
  local tmp="$(mktemp -d)"
  for test_name in "${TEST_NAMES[@]}"; do
    mkdir -p "$tmp/$test_name"
    cd "$tmp/$test_name"
    echo -n "Running $test_name..."
    test"$test_name" >log || {
      echo "FAILED";
      sed -e "s/^/$test_name: /" <log
      rm log
      result=1;
      continue;
    }
    echo "PASSED"
    rm log
  done
  return "$result"
}


main "$@"

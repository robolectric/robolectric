#!/bin/bash
#
# Runs robolectric tests.

set -euo pipefail

# Terminate with a fatal error.
function fatal() {
  echo "Fatal: $*"
  exit 113
}

# Ensures that the given variable is set.
function validate_var() {
  local name="$1"; shift || fatal "Missing argument: name"
  test $# = 0 || fatal "Too many arguments"

  eval [[ -n \${${name}+dummy} ]] || {
    echo "Variable not set: $name";
    return 1;
  }
}

# Ensures that all the required variables are set.
function validate_vars() {
  test $# = 0 || fatal "Too many arguments"

  validate_var 'PRIVATE_INTERMEDIATES'
  validate_var 'PRIVATE_JARS'
  validate_var 'PRIVATE_JAVA_ARGS'
  validate_var 'PRIVATE_ROBOLECTRIC_PATH'
  validate_var 'PRIVATE_ROBOLECTRIC_SCRIPT_PATH'
  validate_var 'PRIVATE_RUN_INDIVIDUALLY'
  validate_var 'PRIVATE_TARGET_MESSAGE'
  validate_var 'PRIVATE_TESTS'
  validate_var 'PRIVATE_TIMEOUT'

  validate_var 'XML_OUTPUT_FILE'
  validate_var 'TEST_WORKSPACE'
}

# Remove leading and trailing spaces around the given argument.
function strip() {
  local value="$1"; shift || fatal "Missing argument: value"
  test $# = 0 || fatal "Too many arguments"

  echo "$value" | sed -e 's/^ *//' -e 's/ *$//'
}

# Normalizes a list of paths and turns it into a colon-separated list.
function normalize-path-list() {
  echo "$@" | sed -e 's/^ *//' -e 's/ *$//' -e 's/  */ /g' -e 's/ /:/g'
}

function junit() {
  # This adds the lib folder to the cp.
  local classpath="$(strip "$(normalize-path-list "${PRIVATE_JARS}")")"
  # Setting the DEBUG_ROBOLECTRIC environment variable will print additional logging from
  # Robolectric and also make it wait for a debugger to be connected.
  # For Android Studio / IntelliJ the debugger can be connected via the "remote" configuration:
  #     https://www.jetbrains.com/help/idea/2016.2/run-debug-configuration-remote.html
  # From command line the debugger can be connected via
  #     jdb -attach localhost:5005
  if [ -n ${DEBUG_ROBOLECTRIC:-""} ]; then
    # The arguments to the JVM needed to debug the tests.
    # - server: wait for connection rather than connecting to a debugger
    # - transport: how to accept debugger connections (sockets)
    # - address: the port on which to accept debugger connections
    # - timeout: how long (in ms) to wait for a debugger to connect
    # - suspend: do not start running any code until the debugger connects
    local debug_java_args="-Drobolectric.logging.enabled=true \
        -Xdebug -agentlib:jdwp=server=y,transport=dt_socket,address=localhost:5005,suspend=y"
    # Remove the timeout so Robolectric doesn't get killed while debugging
    local debug_timeout="0"
  fi
  local command=(
    "${PRIVATE_ROBOLECTRIC_SCRIPT_PATH}/java-timeout"
    "${debug_timeout:-${PRIVATE_TIMEOUT}}"
    ${debug_java_args:-${PRIVATE_JAVA_ARGS}}
    -Drobolectric.dependency.dir="${PRIVATE_ROBOLECTRIC_PATH}"
    -Drobolectric.offline=true
    -Drobolectric.logging=stdout
    -cp "$classpath"
    com.android.junitxml.JUnitXmlRunner
  )
  echo "${command[@]}" "$@"
  "${command[@]}" "$@"
}

function runtests() {
  local tests="$1"; shift || fatal "Missing argument: tests"
  test $# = 0 || fatal "Too many arguments"

  if [[ "$(strip "${PRIVATE_RUN_INDIVIDUALLY}")" = 'true' ]]; then
    local result=0
    for test in ${tests}; do
      echo "-------------------------------------------------------------------"
      echo "Running $test:"
      junit "${test}"
    done
    return "$result"
  else
    echo "-------------------------------------------------------------------"
    echo "Running $tests:"
    junit $tests  # Contains a space-separated list of tests.
  fi
}

# Run the robolectric tests
function run() {
  test $# = 0 || fatal "Too many arguments"

  [ "${PRIVATE_TARGET_MESSAGE}" == '' ] || echo "${PRIVATE_TARGET_MESSAGE}"
  local tests="${PRIVATE_TESTS}"
  if [ "$tests" = '' ]; then
    # Somehow there are no tests to run. Assume this is failure.
    echo "No tests to run."
    exit 1
  fi
  local output="${PRIVATE_INTERMEDIATES}/output.out"
  local failed="${PRIVATE_INTERMEDIATES}/failed.out"
  local result=0
  runtests "${tests}" >"$output" 2>&1 || result="$?"
  echo "$output"
  cat "$output"
  if [ "$result" = 0 ]; then
    return "$result"
  fi
  "${PRIVATE_ROBOLECTRIC_SCRIPT_PATH}/list_failed.sh" <"$output" >"$failed"
  return "$result"
}

function main() {
  test $# = 0 || fatal "Too many arguments"

  validate_vars
  run
}

main "$@"

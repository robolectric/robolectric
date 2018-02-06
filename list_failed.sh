#!/bin/bash
#
# This script lists the tests which are failing in the output of Robolectric
# tests.
# TODO: Remove this script and move the functionality into a custom JUnit runner.

# Matches the line specifying which test has failed and matches the test name
# and class name as the first and second matching group, respectively.
readonly FAILED_TEST_RE='^[1-9][0-9]*)\s\(\w\+\)(\(\(\w\|.\)\+\))$'

# Fails with a message.
function fatal() {
  echo 1>&2 "FATAL: $@"
  exit 113
}

function main() {
  test $# = 0 || fatal "Too many arguments: $@"

  sed -e '1,/^There \(was 1 failure\|were [0-9]* failures\):$/d' |
      grep "$FAILED_TEST_RE" |
      sed -e "s/$FAILED_TEST_RE/\2.\1/" ||
      true
}

set -e
set -o pipefail
main "$@"
